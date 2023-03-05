package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.StructContext;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.stream.Collectors;

public class StructContextFactory implements IModelContextFactory {
    private ModelResolver modelResolver;
    public StructContextFactory(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    @Override
    public ModelContext makeModelContext(RustType rustType, Schema schema) {
        Map<String, Schema> schemaProperties = schema.getProperties();
        if (null == schemaProperties) {
            return null;
        }

        List<StructContext.Property> properties = (List<StructContext.Property>) schema.getProperties().entrySet().stream()
                .map((s) -> this.toProperty((Map.Entry<String, Schema>) s, schema))
                .collect(Collectors.toList());

        StructContext struct = new StructContext(properties);
        String docComment = schema.getDescription();
        return ModelContext.forStruct(rustType, struct, docComment);
    }

    private StructContext.Property toProperty(Map.Entry<String, Schema> property, Schema model) {
        SnakeCaseName sanitizedName = RustConfig.sanitizePropertyName(property.getKey());
        String serdeName = null;
        if (!sanitizedName.toString().equals(property.getKey())) {
            serdeName = property.getKey();
        }

        RustType dataType = this.modelResolver.resolveSchema(property.getValue());
        List<String> requiredProperties = model.getRequired();
        if (null != requiredProperties && !requiredProperties.contains(property.getKey())) {
            dataType = new RustType(null, new PascalCaseName("Option"), dataType);
        }

        String docComment = property.getValue().getDescription();
        return new StructContext.Property(sanitizedName, dataType, serdeName, docComment);
    }
}
