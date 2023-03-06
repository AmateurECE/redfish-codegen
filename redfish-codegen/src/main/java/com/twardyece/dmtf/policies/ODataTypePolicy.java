package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;

import java.util.Map;

public class ODataTypePolicy implements IModelGenerationPolicy {
    private ODataTypeIdentifier identifier;
    public ODataTypePolicy(ODataTypeIdentifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        // TODO: For each ModelContext that contains a StructContext that contains a property named odata_type,
        //  change the type of this property to monostate::MustBe!("<ActualODataType>")
        // TODO: Delete the model with OpenAPI path #/components/schemas/odata-v4_type
    }
}
