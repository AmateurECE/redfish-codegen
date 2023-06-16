package com.twardyece.dmtf.component;

import com.twardyece.dmtf.component.match.IComponentMatcher;
import io.swagger.v3.oas.models.PathItem;

import java.util.*;

public class PathService {

    private final IComponentMatcher[] componentMatchers;

    public PathService(IComponentMatcher[] componentMatchers) {
        this.componentMatchers = componentMatchers;
    }

    public Iterator<ComponentContext> getComponents(Map<String, PathItem> paths, ComponentTypeTranslationService service) {
        ComponentRepository repository = new ComponentRepository(service);

        List<String> sorted = paths.keySet().stream().sorted().toList();
        Map<String, ComponentContext> components = new HashMap<>();

        // Compose the graph of ApiComponents
        for (String uri : sorted) {
            String path = removeTrailingSlash(uri);
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

        establishRelationships(components, repository);
        return repository.iterator();
    }

    private static void establishRelationships(Map<String, ComponentContext> components, ComponentRepository repository) {
        for (String firstPath : components.keySet()) {
            // Determine the parent path, which is the largest substring from the set of valid paths
            String maxSubstring = "";
            ComponentContext previous = null;
            ComponentContext current = components.get(firstPath);
            for (String secondPath : components.keySet()) {
                if (firstPath.contains(secondPath) && secondPath.length() > maxSubstring.length() && !firstPath.equals(secondPath)) {
                    maxSubstring = secondPath;
                    previous = components.get(secondPath);
                }
            }

            // If we determined that this endpoint has a parent endpoint, add an edge between the two.
            if (previous != null && !previous.equals(current)) {
                repository.owns(previous, current);
            }
        }
    }

    private static String removeTrailingSlash(String path) {
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        return path.substring(0, endIndex);
    }
}
