package com.twardyece.dmtf.component;

import com.twardyece.dmtf.component.match.IComponentMatcher;
import com.twardyece.dmtf.openapi.ModelUtils;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.PathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentMatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("/\\{[A-Za-z0-9_]+}");
    private final IComponentMatcher[] componentMatchers;
    private final PathService pathService;

    public ComponentMatchService(IComponentMatcher[] componentMatchers, PathService pathService) {
        this.componentMatchers = componentMatchers;
        this.pathService = pathService;
    }

    public Iterator<ComponentContext> getComponents(Map<String, PathItem> paths, ComponentRepository repository) {
        List<String> sorted = paths.keySet().stream().sorted().toList();

        // Compose the graph of ApiComponents
        for (String uri : sorted) {
            String path = PathService.removeTrailingSlash(uri);
            Optional<ComponentContext> component = Optional.empty();
            for (IComponentMatcher componentMatcher : componentMatchers) {
                component = componentMatcher.matchUri(repository, uri, paths.get(path));
                if (component.isPresent()) {
                    break;
                }
            }

            if (component.isEmpty()) {
                throw new RuntimeException("No Component Matcher for endpoint " + uri);
            }
        }

        establishComponentRelationships(repository);
        return repository.iterator();
    }

    private void establishComponentRelationships(ComponentRepository repository) {
        Iterator<ComponentContext> iterator = repository.iterator();
        while (iterator.hasNext()) {
            ComponentContext next = iterator.next();
            next.owningComponents = repository.getOwningComponents(next)
                    .stream()
                    .map((context) -> new ComponentContext.Supercomponent(
                            new PascalCaseName(context.rustType.getName()), context.rustType))
                    .toList();

            List<ComponentContext> subcomponents = repository.getSubcomponents(next);
            Map<String, ComponentContext> subcomponentMap = new HashMap<>();
            for (ComponentContext component : subcomponents) {
                for (String path : component.paths) {
                    try {
                        String parentPath = this.pathService.getClosestParent(next.paths, path);
                        String mountpoint = path.substring(parentPath.length());
                        boolean isDirectDescendant = 1 == (mountpoint.length() - mountpoint.replace("/", "").length());
                        if (!isDirectDescendant) {
                            continue;
                        }

                        Matcher matcher = PARAMETER_PATTERN.matcher(mountpoint);
                        if (matcher.find()) {
                            mountpoint = "/:" + new SnakeCaseName(component.rustType.getName()) + "_id";
                        }
                        if (!subcomponentMap.containsKey(mountpoint)) {
                            subcomponentMap.put(mountpoint, component);
                        }
                    } catch (PathService.NoCloseParentException ignored) {
                    }
                }
            }

            if (subcomponentMap.size() != subcomponents.size()) {
                LOGGER.warn("Component " + next.rustType.getName() + " has multiple mount points for one of its subcomponents");
            }

            next.subcomponents = subcomponentMap.entrySet()
                    .stream()
                    .map((entry) -> {
                        SnakeCaseName name;
                        if (entry.getKey().startsWith("/:")) {
                            name = new SnakeCaseName(entry.getValue().rustType.getName());
                        } else {
                            name = CaseConversion.toSnakeCase(entry.getKey().substring(1));
                        }
                        return new ComponentContext.Subcomponent(
                                name,
                                new PascalCaseName(entry.getValue().rustType.getName()),
                                entry.getValue().rustType,
                                entry.getKey());
                    })
                    .toList();
        }
    }
}
