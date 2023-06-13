package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.component.ComponentContext;
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
    public void apply(Graph<ComponentContext, DefaultEdge> graph, ComponentContext root) {
        Iterator<ComponentContext> iterator = new BreadthFirstIterator<>(graph, root);
        List<ComponentContext> maskedComponentContexts = new ArrayList<>();
        while (iterator.hasNext()) {
            ComponentContext context = iterator.next();
            if (context.mountpoints.stream().anyMatch(this.maskedTraits::contains)) {
                maskedComponentContexts.add(context);
            }
        }

        for (ComponentContext context : maskedComponentContexts) {
            graph.removeVertex(context);
        }
    }
}
