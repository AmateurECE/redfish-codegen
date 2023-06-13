package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.component.ComponentContext;
import com.twardyece.dmtf.text.PascalCaseName;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Iterator;
import java.util.Optional;

public class PatchRequestBodyTypePolicy implements IApiGenerationPolicy {
    private static final RustType serdeJson = new RustType(CratePath.parse("serde_json"), new PascalCaseName("Value"));

    public PatchRequestBodyTypePolicy() {}

    @Override
    public void apply(Graph<ComponentContext, DefaultEdge> graph, ComponentContext root) {
        // Translate the requestBody parameter for each trait that has a patch operation to serde_json::Value.
        Iterator<ComponentContext> iterator = new DepthFirstIterator<>(graph, root);
        while (iterator.hasNext()) {
            ComponentContext trait = iterator.next();
            Optional<ComponentContext.Operation> patch = trait.operations
                    .stream()
                    .filter((o) -> "patch".equals(o.name))
                    .findFirst();

            if (patch.isPresent()) {
                ComponentContext.Parameter requestBody = patch.get().parameters
                        .stream()
                        .filter((p) -> "body".equals(p.name()))
                        .findFirst()
                        .get();

                requestBody.rustType = serdeJson;
            }
        }
    }
}
