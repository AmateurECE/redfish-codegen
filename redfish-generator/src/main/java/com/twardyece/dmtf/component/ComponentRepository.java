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
    private final ComponentTypeTranslationService componentTypeTranslationService;
    private final PathService pathService;
    private final RustType baseRegistry;
    private final Graph<ComponentContext, DefaultEdge> graph;
    private ComponentContext root;
    private final Map<String, ComponentContext> componentsByRef;
    private final Map<String, ComponentContext> componentsByPath;

    public ComponentRepository(ComponentTypeTranslationService componentTypeTranslationService, PathService pathService, RustType baseRegistry) {
        this.componentTypeTranslationService = componentTypeTranslationService;
        this.pathService = pathService;
        this.baseRegistry = baseRegistry;
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.componentsByRef = new HashMap<>();
        this.componentsByPath = new HashMap<>();
    }

    public ComponentContext getOrCreateComponent(String componentRef, String path) {
        if (this.componentsByRef.containsKey(componentRef)) {
            return this.componentsByRef.get(componentRef);
        }

        RustType rustType = this.componentTypeTranslationService.getRustTypeForComponentName(componentRef);
        ComponentContext component = new ComponentContext(rustType, this.baseRegistry);

        this.graph.addVertex(component);
        this.componentsByRef.put(componentRef, component);
        this.componentsByPath.put(path, component);

        if (null == this.root) {
            this.root = component;
        } else {
            this.graph.addEdge(this.getComponentParentOfPath(path), component);
        }

        return component;
    }

    public ComponentContext getComponentParentOfPath(String path) {
        String mountpoint = this.pathService.getMountpoint(this.componentsByPath.keySet(), path);
        return this.componentsByPath.get(mountpoint);
    }

    public Iterator<ComponentContext> iterator() {
        return new DepthFirstIterator<>(this.graph, this.root);
    }
}
