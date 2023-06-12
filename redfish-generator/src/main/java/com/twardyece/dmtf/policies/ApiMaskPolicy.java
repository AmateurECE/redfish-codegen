package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.component.TraitContext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApiMaskPolicy implements IApiGenerationPolicy {
    private final List<String> maskedTraits;

    public ApiMaskPolicy(List<String> maskedTraits) {
        this.maskedTraits = maskedTraits;
    }

    @Override
    public void apply(Graph<TraitContext, DefaultEdge> graph, TraitContext root) {
        Iterator<TraitContext> iterator = new BreadthFirstIterator<>(graph, root);
        List<TraitContext> maskedTraitContexts = new ArrayList<>();
        while (iterator.hasNext()) {
            TraitContext context = iterator.next();
            if (context.mountpoints.stream().anyMatch(this.maskedTraits::contains)) {
                maskedTraitContexts.add(context);
            }
        }

        for (TraitContext context : maskedTraitContexts) {
            graph.removeVertex(context);
        }
    }
}
