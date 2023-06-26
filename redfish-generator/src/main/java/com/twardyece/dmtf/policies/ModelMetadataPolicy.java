package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.specification.JsonSchemaIdentifier;
import com.twardyece.dmtf.model.context.Metadata;
import com.twardyece.dmtf.model.context.ModelContext;

import java.util.Map;
import java.util.Optional;

public class ModelMetadataPolicy implements IModelGenerationPolicy {
    private final JsonSchemaIdentifier jsonSchemaIdentifier;

    public ModelMetadataPolicy(JsonSchemaIdentifier jsonSchemaIdentifier) {
        this.jsonSchemaIdentifier = jsonSchemaIdentifier;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        for (Map.Entry<String, ModuleFile<ModelContext>> entry : models.entrySet()) {
            Optional<String> jsonSchemaIdentifier = this.jsonSchemaIdentifier.identify(entry.getKey());
            if (jsonSchemaIdentifier.isEmpty()) {
                throw new RuntimeException("No matching json-schema file for identifier " + entry.getKey());
            }
            entry.getValue().getContext().metadata = new Metadata(jsonSchemaIdentifier.get());
        }
    }
}
