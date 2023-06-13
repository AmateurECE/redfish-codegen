package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.component.ComponentContext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public interface IApiGenerationPolicy {
    void apply(Graph<ComponentContext, DefaultEdge> graph, ComponentContext root);
}
