package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.api.TraitContext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

public interface IApiGenerationPolicy {
    void apply(Graph<TraitContext, DefaultEdge> graph, TraitContext root);
}
