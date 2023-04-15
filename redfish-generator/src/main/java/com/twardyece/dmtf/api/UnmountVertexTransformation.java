package com.twardyece.dmtf.api;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class UnmountVertexTransformation {
    private static final Pattern collectionMemberPattern = Pattern.compile("^\\{[A-Za-z0-9]+\\}$");
    // If the need to implement additional transformations arises, we can make these methods an interface.
    public boolean check(Graph<ApiEndpoint, DefaultEdge> graph, ApiEndpoint endpoint) {
        return 1 < graph.inDegreeOf(endpoint);
    }

    public void perform(Graph<ApiEndpoint, DefaultEdge> graph, ApiEndpoint endpoint, ApiEndpoint mountpoint) {
        // Remove all edges between this node and its predecessors, "moving" the node up to level 0 in the graph.
        ArrayList<DefaultEdge> edges = new ArrayList<>();
        edges.addAll(graph.incomingEdgesOf(endpoint));
        graph.removeAllEdges(edges);

        // Add an edge between this endpoint and the root node, so that this node is still reachable during normal
        // traversal.
        graph.addEdge(mountpoint, endpoint);
    }
}
