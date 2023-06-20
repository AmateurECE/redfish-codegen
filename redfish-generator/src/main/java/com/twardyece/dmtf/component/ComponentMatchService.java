package com.twardyece.dmtf.component;

import com.twardyece.dmtf.component.match.IComponentMatcher;
import io.swagger.v3.oas.models.PathItem;

import java.util.*;

public class ComponentMatchService {

    private final IComponentMatcher[] componentMatchers;

    public ComponentMatchService(IComponentMatcher[] componentMatchers) {
        this.componentMatchers = componentMatchers;
    }

    public Iterator<ComponentContext> getComponents(Map<String, PathItem> paths, ComponentRepository repository) {
        List<String> sorted = paths.keySet().stream().sorted().toList();
        Map<String, ComponentContext> components = new HashMap<>();

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

            components.put(path, component.get());
        }

        return repository.iterator();
    }
}
