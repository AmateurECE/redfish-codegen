package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.StructContext;

import java.util.Map;

public class ODataTypePolicy implements IModelGenerationPolicy {
    private ODataTypeIdentifier identifier;
    public ODataTypePolicy(ODataTypeIdentifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        // For each ModelContext that contains a StructContext that contains a property named odata_type,
        //  change the type of this property to monostate::MustBe!("<ActualODataType>")
        for (Map.Entry<String, ModuleFile<ModelContext>> entry : models.entrySet()) {
            StructContext struct = entry.getValue().getContext().structContext;
            if (null != struct) {
                for (StructContext.Property property : struct.properties) {
                    if ("odata_type".equals(property.name())) {
                        String odataType = this.identifier.identify(entry.getKey());
                        property.setTypeOverride("monostate::MustBe!(\"" + odataType + "\")");
                    }
                }
            }
        }

        // Delete the model with OpenAPI path #/components/schemas/odata-v4_type
        models.remove("odata-v4_type");
    }
}
