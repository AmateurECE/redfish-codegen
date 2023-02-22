package com.twardyece.dmtf.api;

import com.twardyece.dmtf.api.mapper.IApiFileMapper;
import io.swagger.v3.oas.models.PathItem;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PathMap {
    private Graph<ApiEndpoint, DefaultEdge> graph;
    private Map<String, ApiEndpoint> endpoints;
    ApiEndpoint root;
    private static final PathNameTranslator pathName = new PathNameTranslator();

    public PathMap(HashMap<String, PathItem> paths) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.endpoints = new HashMap<>();

        List<String> sorted = paths.keySet().stream().sorted().collect(Collectors.toList());
        for (String path : sorted) {
            addEndpoint(path, paths.get(path));
        }

        this.normalize();
    }

    private void addEndpoint(String path, PathItem pathItem) {
        // TODO: Change the structure of the graph up
        // vertices are API endpoints (not a mix of path components and API endpoints), and edges flow to API endpoints
        // at sub-paths of other API endpoints.
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        path = path.substring(0, endIndex);

        String name = pathName.translate(path, pathItem);
        ApiEndpoint current = new ApiEndpoint(name, path, pathItem);
        this.graph.addVertex(current);
        this.endpoints.put(path, current);

        if (2 < this.endpoints.size()) {
            String maxSubstring = "";
            for (String endpoint : this.endpoints.keySet()) {
                if (path.contains(endpoint) && endpoint.length() > maxSubstring.length()) {
                    maxSubstring = endpoint;
                }
            }

            ApiEndpoint previous = this.endpoints.get(maxSubstring);
            this.graph.addEdge(previous, current);
        } else {
            this.root = current;
        }
    }

    private void normalize() {
        List<ApiEndpoint> mountedEndpoints = new ArrayList<>();
        UnmountPathTransformation transformation = new UnmountPathTransformation();
        Iterator<ApiEndpoint> iterator = new DepthFirstIterator<>(this.graph, this.root);
        while (iterator.hasNext()) {
            ApiEndpoint endpoint = iterator.next();
            if (transformation.check(this.graph, endpoint)) {
                mountedEndpoints.add(endpoint);
            }
        }

        for (ApiEndpoint endpoint : mountedEndpoints) {
            transformation.perform(this.graph, endpoint, this.root);
        }
    }

    public List<ApiTrait> getTraits(ApiResolver resolver) {
        List<ApiTrait> traits = new ArrayList<>();
        Iterator<ApiEndpoint> iterator = new DepthFirstIterator<>(this.graph, this.root);
        while (iterator.hasNext()) {
            ApiEndpoint endpoint = iterator.next();
            String path = PathMap.getPaths(this.graph, this.root, endpoint).get(0);
            IApiFileMapper.ApiMatchResult result = resolver.resolve(path);
            traits.add(new ApiTrait(result.path, result.name, endpoint.getPathItem()));
        }

        return traits;
    }

    public static List<String> getPaths(Graph<ApiEndpoint, DefaultEdge> graph, ApiEndpoint start, ApiEndpoint finish) {
        AllDirectedPaths<ApiEndpoint, DefaultEdge> pathFinder = new AllDirectedPaths<>(graph);
        List<GraphPath<ApiEndpoint, DefaultEdge>> paths = pathFinder.getAllPaths(start, finish, true, null);
        return paths.stream()
                .map((path) -> String.join("/", path.getVertexList().stream().map((vertex) -> vertex.getName()).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
