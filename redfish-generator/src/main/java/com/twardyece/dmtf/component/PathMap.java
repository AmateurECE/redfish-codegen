package com.twardyece.dmtf.component;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.component.match.IComponentMatcher;
import com.twardyece.dmtf.model.ModelResolver;
import io.swagger.v3.oas.models.PathItem;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;

public class PathMap {
    private final List<ComponentContext> components;

    public PathMap(HashMap<String, PathItem> paths, ComponentContextFactory factory, ModelResolver resolver, IComponentMatcher[] componentMatchers) {
        Graph<ApiComponent, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        ApiComponent root = null;
        List<String> sorted = paths.keySet().stream().sorted().toList();
        Map<String, ApiComponent> components = new HashMap<>();

        // Compose the graph of ApiComponents
        for (String uri : sorted) {
            String path = removeTrailingSlash(uri);
            RustType component = getComponentType(componentMatchers, resolver, path);
            ApiComponent current = new ApiComponent(component, paths.get(path));

            // Add it to the graph, and associate it with this endpoint
            graph.addVertex(current);
            components.put(path, current);

            // Determine the parent path, which is the largest substring from the set of valid paths
            String maxSubstring = "";
            ApiComponent previous = null;
            for (String endpoint : sorted) {
                if (path.contains(endpoint) && endpoint.length() > maxSubstring.length() && !path.equals(endpoint)) {
                    maxSubstring = endpoint;
                    previous = components.get(endpoint);
                }
            }

            // If we determined that this endpoint has a parent endpoint, add an edge between the two.
            if (previous != null && !previous.equals(current)) {
                graph.addEdge(previous, current);
            }

            if (null == root) {
                root = current;
            }
        }

        this.components = transformGraphToComponentList(graph, root, factory);
    }

    private RustType getComponentType(IComponentMatcher[] componentMatchers, ModelResolver resolver, String uri) {
        for (IComponentMatcher componentMatcher : componentMatchers) {
            Optional<String> component = componentMatcher.getComponent(uri);
            if (component.isPresent()) {
                return resolver.resolvePath(component.get());
            }
        }

        throw new RuntimeException("No Component Matcher for endpoint " + uri);
    }

    public Iterator<ComponentContext> getComponentIterator() {
        return this.components.listIterator();
    }

    private String removeTrailingSlash(String path) {
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        return path.substring(0, endIndex);
    }

    private static List<ComponentContext> transformGraphToComponentList(Graph<ApiComponent, DefaultEdge> graph, ApiComponent root, ComponentContextFactory factory) {
        List<ComponentContext> components = new ArrayList<>();
        Iterator<ApiComponent> iterator = new DepthFirstIterator<>(graph, root);
        while (iterator.hasNext()) {
            ApiComponent next = iterator.next();
            List<RustType> subordinateComponents = Graphs.successorListOf(graph, next).stream().map((api) -> api.rustType).toList();
            List<RustType> owningComponents = Graphs.predecessorListOf(graph, next).stream().map((api) -> api.rustType).toList();
            components.add(factory.makeComponentContext(next.rustType, next.pathItem, subordinateComponents, owningComponents));
        }

        return components;
    }
}
