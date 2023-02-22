package com.twardyece.dmtf.api;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UnmountPathTransformation {
    private static final Pattern collectionMemberPattern = Pattern.compile("^\\{[A-Za-z0-9]+\\}$");
    // If the need to implement additional transformations arises, we can make these methods an interface.
    public boolean check(Graph<ApiEndpoint, DefaultEdge> graph, ApiEndpoint endpoint) {
        // TODO: Can we get rid of this?
        //        boolean isCollectionMemberEndpoint = collectionMemberPattern.matcher(endpoint.getName()).matches();
        boolean hasMultiplePredecessors = 1 < graph.inDegreeOf(endpoint);
        //        return hasMultiplePredecessors && !isCollectionMemberEndpoint;
        return hasMultiplePredecessors;
    }

    public void perform(Graph<ApiEndpoint, DefaultEdge> graph, ApiEndpoint endpoint, ApiEndpoint mountpoint) {
        // The paths from the root node to endpoint form the valid mountpoints. Traverse them and store them in the
        // current endpoint.
        endpoint.setValidMountpoints(Graphs.predecessorListOf(graph, endpoint).stream()
                .map((e) -> e.getPath()).collect(Collectors.toList()));

        // Remove all edges between this node and its predecessors, "moving" the node up to level 0 in the graph.
        ArrayList<DefaultEdge> edges = new ArrayList<>();
        edges.addAll(graph.incomingEdgesOf(endpoint));
        graph.removeAllEdges(edges);

        // Add an edge between this endpoint and the root node, so that this node is still reachable during normal
        // traversal.
        graph.addEdge(mountpoint, endpoint);
    }
}
