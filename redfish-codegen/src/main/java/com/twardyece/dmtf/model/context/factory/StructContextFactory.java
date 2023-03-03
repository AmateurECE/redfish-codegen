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

        // Get imports from properties
        Set<CratePath> importSet = new HashSet<>();
        for (StructContext.Property property : properties) {
            addImports(importSet, property.rustType);
        }

        List<ModelContext.Import> imports = importSet.stream().map(ModelContext.Import::new).toList();

        StructContext struct = new StructContext(properties);
        String docComment = schema.getDescription();
        return ModelContext.forStruct(rustType, struct, imports, docComment);
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

    // TODO: Could probably move this import logic into a shared location to be used by both this and TraitContextFactory
    private static void addImports(Set<CratePath> imports, RustType rustType) {
        // First, handle any inner types
        if (null != rustType.getInnerType()) {
            addImports(imports, rustType.getInnerType());
        }

        if (!rustType.isPrimitive() && rustType.getPath().isCrateLocal()) {
            List<SnakeCaseName> components = rustType.getPath().getComponents();
            if (components.size() > 1) {
                List<SnakeCaseName> front = components.subList(0, 2);
                List<SnakeCaseName> back = components.subList(1, components.size());
                CratePath importPath = CratePath.relative(front);
                imports.add(importPath);
                rustType.setImportPath(CratePath.relative(back));
            }
        }
    }
}
