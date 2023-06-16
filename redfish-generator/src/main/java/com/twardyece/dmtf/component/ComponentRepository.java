package com.twardyece.dmtf.component;

import com.twardyece.dmtf.RustType;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ComponentRepository {
    private final Graph<ComponentContext, DefaultEdge> graph;
    private ComponentContext root;
    private final Map<String, ComponentContext> components;
    private final ComponentTypeTranslationService service;

    public ComponentRepository(ComponentTypeTranslationService service) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.components = new HashMap<>();
        this.service = service;
    }

    public ComponentContext getOrCreateComponent(String componentRef) {
        if (this.components.containsKey(componentRef)) {
            return this.components.get(componentRef);
        }

        RustType rustType = this.service.getRustTypeForComponentName(componentRef);
        ComponentContext component = new ComponentContext(rustType);

        this.graph.addVertex(component);
        this.components.put(componentRef, component);

        if (null == this.root) {
            this.root = component;
        }

        return component;
    }

    public void owns(ComponentContext owner, ComponentContext subordinate) {
        this.graph.addEdge(owner, subordinate);
    }

    public Iterator<ComponentContext> iterator() {
        return new DepthFirstIterator<>(this.graph, this.root);
    }
}
