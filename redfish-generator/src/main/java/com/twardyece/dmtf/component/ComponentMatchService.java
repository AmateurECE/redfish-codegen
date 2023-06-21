package com.twardyece.dmtf.component;

import com.twardyece.dmtf.component.match.IComponentMatcher;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ComponentMatchService {

    private final IComponentMatcher[] componentMatchers;

    public ComponentMatchService(IComponentMatcher[] componentMatchers) {
        this.componentMatchers = componentMatchers;
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

    private static void establishComponentRelationships(ComponentRepository repository) {
        Iterator<ComponentContext> iterator = repository.iterator();
        while (iterator.hasNext()) {
            ComponentContext next = iterator.next();
            next.owningComponents = repository.getOwningComponents(next)
                    .stream()
                    .map((context) -> new ComponentContext.Supercomponent(new PascalCaseName(context.rustType.getName()), context.rustType))
                    .toList();
        }
    }
}
