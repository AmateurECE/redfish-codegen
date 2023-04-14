package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.StructContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class PropertyDefaultValueOverridePolicy implements IModelGenerationPolicy {
    private Map<Pair<String, String>, String> overrides;

    public PropertyDefaultValueOverridePolicy(Map<Pair<String, String>, String> overrides) {
        this.overrides = overrides;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        for (Map.Entry<Pair<String, String>, String> entry : overrides.entrySet()) {
            StructContext.Property property = models.get(entry.getKey().getLeft()).getContext().structContext.properties
                    .stream()
                    .filter((p) -> p.name().equals(entry.getKey().getRight()))
                    .findFirst()
                    .get();

            property.setDefaultValue(entry.getValue());
        }
    }
}
