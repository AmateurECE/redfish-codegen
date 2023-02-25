package com.twardyece.dmtf.model;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
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
    static final Pattern reservedCharactersInFirstPosition = Pattern.compile("^@");
    static final Pattern invalidCharacters = Pattern.compile("[@.]");

    private ModelResolver modelResolver;
    public ModelContextFactory(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    public ModelContext makeModelContext(PascalCaseName name, Schema schema) {
        SnakeCaseName modelModule = new SnakeCaseName(name);
        if ("".equals(modelModule.toString())) {
            LOGGER.warn("modelModule is empty for model " + schema.getName());
        }

        Map<String, Schema> schemaProperties = schema.getProperties();
        List<StructContext.Property> properties = null;
        List<ModelContext.Import> imports = null;
        if (null != schemaProperties) {
            properties = (List<StructContext.Property>) schema.getProperties().entrySet().stream()
                    .map((s) -> this.toProperty((Map.Entry<String, Schema>) s))
                    .collect(Collectors.toList());

            // Get imports from properties
            Set<CratePath> importSet = new HashSet<>();
            for (StructContext.Property property : properties) {
                addImports(importSet, property.rustType);
            }

            imports = importSet.stream().map(ModelContext.Import::new).toList();
        }

        StructContext struct = new StructContext(properties);
        return ModelContext.struct(name, modelModule, struct, imports);
    }

    private StructContext.Property toProperty(Map.Entry<String, Schema> property) {
        SnakeCaseName sanitizedName = sanitizePropertyName(property.getKey());
        String serdeType = null;
        if (!sanitizedName.toString().equals(property.getKey())) {
            serdeType = property.getKey();
        }
        return new StructContext.Property(sanitizedName, this.modelResolver.resolveType(property.getValue()), serdeType);
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

    private static SnakeCaseName sanitizePropertyName(String name) {
        List<SnakeCaseName> safeName = Arrays.stream(
                        replaceInvalidCharacters(
                                removeReservedCharactersInFirstPosition(name))
                                .split(" "))
                .map((identifier) -> CaseConversion.toSnakeCase(identifier))
                .collect(Collectors.toList());
        return RustConfig.escapeReservedKeyword(new SnakeCaseName(safeName));
    }

    private static String removeReservedCharactersInFirstPosition(String name) {
        Matcher matcher = reservedCharactersInFirstPosition.matcher(name);
        if (matcher.find()) {
            return matcher.replaceFirst("");
        } else {
            return name;
        }
    }

    private static String replaceInvalidCharacters(String name) {
        Matcher matcher = invalidCharacters.matcher(name);
        if (matcher.find()) {
            return matcher.replaceAll(" ");
        } else {
            return name;
        }
    }
}
