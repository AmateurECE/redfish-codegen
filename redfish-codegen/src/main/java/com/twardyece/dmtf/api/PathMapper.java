package com.twardyece.dmtf.api;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class PathMapper {
    private Graph<ApiEndpoint, DefaultEdge> graph;

    public PathMapper() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    public void addEndpoint(String path) {
        int startIndex = 0, endIndex = path.length();
        if ('/' == path.charAt(0)) {
            startIndex = 1;
        }

        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        String[] components = path.substring(startIndex, endIndex).split("/");
        if (2 > components.length) {
            return;
        }

        ApiEndpoint previous = new ApiEndpoint(components[0]);
        this.graph.addVertex(previous);
        for (int i = 1; i < components.length; ++i) {
            ApiEndpoint current = new ApiEndpoint(components[i]);
            this.graph.addVertex(current);
            this.graph.addEdge(previous, current);
            previous = current;
        }
    }

    private class ApiEndpoint implements Comparable<ApiEndpoint> {
        public ApiEndpoint(String name) { this.name = name; }
        String name;

        @Override
        public String toString() { return this.name; }
        @Override
        public int compareTo(ApiEndpoint o) {
            return this.name.compareTo(o.name);
        }
        @Override
        public int hashCode() { return this.name.hashCode(); }
        @Override
        public boolean equals(Object o) {
            if (o instanceof ApiEndpoint) {
                return this.name.equals(((ApiEndpoint) o).name);
            } else {
                return false;
            }
        }
    }
}
