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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PathMap {
    private Graph<ApiEndpoint, DefaultEdge> graph;
    private static VertexNamespaceMultiplexer mux = new VertexNamespaceMultiplexer("Actions",
            Pattern.compile("(?<=Actions/)([A-Za-z0-9]+).(?<name>[A-Za-z0-9]+)"), "name");
    private static ApiEndpoint mountpoint = ApiEndpoint.component("mnt");

    public PathMap() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.graph.addVertex(PathMap.mountpoint);
    }

    public void addEndpoint(String path, PathItem pathItem) {
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        String[] components = path.substring(0, endIndex).split("/");
        if (2 > components.length) {
            return;
        }

        ApiEndpoint previous = ApiEndpoint.component(components[0]);
        this.graph.addVertex(previous);
        for (int i = 1; i < components.length; ++i) {
            String name = this.mux.mux(components[i], path);
            ApiEndpoint current = ApiEndpoint.component(name);
            this.graph.addVertex(current);
            this.graph.addEdge(previous, current);
            previous = current;
        }

        ApiEndpoint endpoint = ApiEndpoint.endpoint(path, pathItem);
        this.graph.addVertex(endpoint);
        this.graph.addEdge(previous, endpoint);
    }

    public void normalize() {
        List<ApiEndpoint> mountedEndpoints = new ArrayList<>();
        UnmountPathTransformation transformation = new UnmountPathTransformation();
        Iterator<ApiEndpoint> iterator = new DepthFirstIterator<>(this.graph, ApiEndpoint.root());
        while (iterator.hasNext()) {
            ApiEndpoint endpoint = iterator.next();
            if (transformation.check(this.graph, endpoint)) {
                mountedEndpoints.add(endpoint);
            }
        }

        for (ApiEndpoint endpoint : mountedEndpoints) {
            transformation.perform(this.graph, endpoint, PathMap.mountpoint);
        }
    }

    public List<ApiTrait> getTraits(ApiResolver resolver) {
        List<ApiTrait> traits = new ArrayList<>();
        ApiEndpoint[] roots = new ApiEndpoint[2];
        roots[0] = ApiEndpoint.root();
        roots[1] = PathMap.mountpoint;
        for (ApiEndpoint root : roots) {
            Iterator<ApiEndpoint> iterator = new DepthFirstIterator<>(this.graph, root);
            while (iterator.hasNext()) {
                ApiEndpoint endpoint = iterator.next();
                if (!endpoint.isEndpoint()) {
                    continue;
                }

                ApiEndpoint predecessor = Graphs.predecessorListOf(this.graph, endpoint).get(0);
                List<String> paths = PathMap.getPaths(this.graph, root, predecessor);
                for (String path : paths) {
                    IApiFileMapper.ApiMatchResult result = resolver.resolve(path);
                    traits.add(new ApiTrait(result.path, result.name, endpoint.getPathItem()));
                }
            }
        }

        return traits;
    }

    public static List<String> getPaths(Graph<ApiEndpoint, DefaultEdge> graph, ApiEndpoint start, ApiEndpoint finish) {
        AllDirectedPaths<ApiEndpoint, DefaultEdge> pathFinder = new AllDirectedPaths<>(graph);
        List<GraphPath<ApiEndpoint, DefaultEdge>> paths = pathFinder.getAllPaths(start, finish, true, null);
        return paths.stream()
                .map((path) -> String.join("/", path.getVertexList().stream().map((vertex) -> PathMap.mux.demux(vertex.getName())).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
