package com.twardyece.dmtf.api;

import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
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
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        path = path.substring(0, endIndex);

        String name = pathName.translate(path, pathItem);
        // Use the last component of the path as the "summary".
        String[] components = path.split("/");
        ApiEndpoint current = new ApiEndpoint(name, components[components.length - 1], pathItem);
        this.graph.addVertex(current);

        if (!this.endpoints.isEmpty()) {
            String maxSubstring = "";
            for (String endpoint : this.endpoints.keySet()) {
                if (path.contains(endpoint) && endpoint.length() > maxSubstring.length() && !path.equals(endpoint)) {
                    maxSubstring = endpoint;
                }
            }

            ApiEndpoint previous = this.endpoints.get(maxSubstring);
            if (previous != null && !previous.equals(current)) {
                this.graph.addEdge(previous, current);
            }
        } else {
            this.root = current;
        }

        this.endpoints.put(path, current);
    }

    private void normalize() {
        List<ApiEndpoint> mountedEndpoints = new ArrayList<>();
        UnmountVertexTransformation transformation = new UnmountVertexTransformation();
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

    public List<TraitContext> getTraits(EndpointResolver resolver, Map<PascalCaseName, PascalCaseName> traitNameOverrides) {
        Map<String, TraitContext> traits = new HashMap<>();

        // Create a mapping from endpoint to path, which allows us to determine the valid mountpoints for a trait
        Map<String, List<String>> mountpoints = new HashMap<>();
        this.endpoints.values().forEach((v) -> mountpoints.put(v.toString(), new ArrayList<>()));
        for (Map.Entry<String, ApiEndpoint> entry : this.endpoints.entrySet()) {
            mountpoints.get(entry.getValue().toString()).add(entry.getKey());
        }

        AllDirectedPaths<ApiEndpoint, DefaultEdge> pathFinder = new AllDirectedPaths<>(this.graph);

        Iterator<ApiEndpoint> iterator = new DepthFirstIterator<>(this.graph, this.root);
        while (iterator.hasNext()) {
            ApiEndpoint endpoint = iterator.next();

            List<GraphPath<ApiEndpoint, DefaultEdge>> paths = pathFinder.getAllPaths(this.root, endpoint, true, null);
            List<List<String>> apiPaths = paths.stream()
                    .map((p) -> p.getVertexList()
                            .stream()
                            .map((vertex) -> vertex.getSummary())
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
            if (apiPaths.size() > 1) {
                throw new RuntimeException("Normalization failed. Endpoint " + endpoint + " has multiple paths from root.");
            }

            EndpointResolver.ApiMatchResult result = resolver.resolve(apiPaths.get(0));
            if (traitNameOverrides.containsKey(result.name)) {
                result.name = traitNameOverrides.get(result.name);
            }

            // TODO: Currently, mountpoints will get all of the mountpoints listed from the openapi specification.
            //      maybe we only want to report mountpoints that correspond to pre-normalized graph edges?
            TraitContext trait = new TraitContext(result.path, result.name, endpoint.getPathItem(),
                    mountpoints.get(endpoint.toString()));
            traits.put(endpoint.toString(), trait);

            // Add this trait as a submodule to the preceding traits
            Graphs.predecessorListOf(this.graph, endpoint)
                    .forEach((e) -> traits.get(e.toString()).submodulePaths.add(trait.path.getLastComponent()));
        }

        return traits.values().stream().toList();
    }
}
