package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.routing.TraitContext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public interface IApiGenerationPolicy {
    void apply(Graph<TraitContext, DefaultEdge> graph, TraitContext root);
}
