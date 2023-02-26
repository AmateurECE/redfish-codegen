package com.twardyece.dmtf.model;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModelContextFactory {
    static final Logger LOGGER = LoggerFactory.getLogger(ModelFile.class);

    private ModelResolver modelResolver;
    public ModelContextFactory(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    public ModelContext makeModelContext(PascalCaseName name, Schema schema) {
        SnakeCaseName modelModule = new SnakeCaseName(name);
        if ("".equals(modelModule.toString())) {
            LOGGER.warn("modelModule is empty for model " + schema.getName());
        }

        if (null != schema.getEnum()) {
            return makeEnum(name, modelModule, schema);
        } else {
            return makeStruct(name, modelModule, schema);
        }
    }

    private ModelContext makeEnum(PascalCaseName name, SnakeCaseName modelModule, Schema schema) {
        Map<String, String> docComments = new HashMap<>();
        Map<String, Map<String, String>> extensions = schema.getExtensions();
        if (extensions.containsKey("x-enumDescriptions")) {
            for (Map.Entry<String, String> value : extensions.get("x-enumDescriptions").entrySet()) {
                docComments.put(value.getKey(), value.getValue());
            }
        }

        if (extensions.containsKey("x-enumLongDescriptions")) {
            for (Map.Entry<String, String> value : extensions.get("x-enumLongDescriptions").entrySet()) {
                docComments.put(value.getKey(), value.getValue());
            }
        }

        if (extensions.containsKey("x-enumVersionAdded")) {
            for (Map.Entry<String, String> value : extensions.get("x-enumVersionAdded").entrySet()) {
                String added = "Added in version " + value.getValue() + ".";
                String existing = docComments.getOrDefault(value.getKey(), null);
                if (null != existing) {
                    docComments.put(value.getKey(), existing + " " + added);
                } else {
                    docComments.put(value.getKey(), added);
                }
            }
        }

        List<EnumContext.Variant> variants = (List<EnumContext.Variant>) schema.getEnum().stream()
                .map((s) -> makeVariant((String) s, docComments.getOrDefault(s, null)))
                .collect(Collectors.toList());
        return ModelContext.forEnum(name, modelModule, new EnumContext(variants), null, schema.getDescription());
    }

    private static EnumContext.Variant makeVariant(String value, String docComment) {
        RustIdentifier name = new RustIdentifier(RustConfig.sanitizeIdentifier(value));
        String serdeName = null;
        if (!name.toString().equals(value)) {
            serdeName = value;
        }

        return new EnumContext.Variant(name, serdeName, docComment);
    }

    private ModelContext makeStruct(PascalCaseName name, SnakeCaseName modelModule, Schema schema) {
        List<StructContext.Property> properties = null;
        List<ModelContext.Import> imports = null;
        Map<String, Schema> schemaProperties = schema.getProperties();
        if (null != schemaProperties) {
            properties = (List<StructContext.Property>) schema.getProperties().entrySet().stream()
                    .map((s) -> this.toProperty((Map.Entry<String, Schema>) s, schema))
                    .collect(Collectors.toList());

            // Get imports from properties
            Set<CratePath> importSet = new HashSet<>();
            for (StructContext.Property property : properties) {
                addImports(importSet, property.rustType);
            }

            imports = importSet.stream().map(ModelContext.Import::new).toList();
        }

        StructContext struct = new StructContext(properties);
        String docComment = schema.getDescription();
        return ModelContext.forStruct(name, modelModule, struct, imports, docComment);
    }

    private StructContext.Property toProperty(Map.Entry<String, Schema> property, Schema model) {
        SnakeCaseName sanitizedName = RustConfig.sanitizePropertyName(property.getKey());
        String serdeName = null;
        if (!sanitizedName.toString().equals(property.getKey())) {
            serdeName = property.getKey();
        }

        RustType dataType = this.modelResolver.resolveType(property.getValue());
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
