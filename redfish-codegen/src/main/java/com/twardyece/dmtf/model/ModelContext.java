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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModelContext {
    String name;
    List<Property> properties;
    SnakeCaseName modelModule;
    List<Import> imports;

    static final Logger LOGGER = LoggerFactory.getLogger(ModelFile.class);
    static final Pattern reservedCharactersInFirstPosition = Pattern.compile("^@");
    static final Pattern invalidCharacters = Pattern.compile("[@.]");

    public ModelContext(PascalCaseName name, Schema schema, ModelResolver resolver) {
        this.name = name.toString();
        this.modelModule = new SnakeCaseName(name);
        if ("".equals(modelModule.toString())) {
            LOGGER.warn("modelModule is empty for model " + schema.getName());
        }

        Map<String, Schema> properties = schema.getProperties();
        if (null == properties) {
            return;
        }

        this.properties = (List<Property>) schema.getProperties().entrySet().stream()
                .map((s) -> toProperty((Map.Entry<String, Schema>)s, resolver))
                .collect(Collectors.toList());

        // Get imports from properties
        ArrayList<String> imports = new ArrayList<>();
        for (Property property : this.properties) {
            addImports(imports, property.getRustType());
        }

        this.imports = imports.stream().sorted().distinct().map((path) -> new Import(path)).collect(Collectors.toList());
    }

    public SnakeCaseName getModule() { return this.modelModule; }

    private static Property toProperty(Map.Entry<String, Schema> property, ModelResolver resolver) {
        SnakeCaseName sanitizedName = sanitizePropertyName(property.getKey());
        String serdeType = null;
        if (!sanitizedName.toString().equals(property.getKey())) {
            serdeType = property.getKey();
        }
        return new Property(sanitizedName, resolver.resolveType(property.getValue()), serdeType);
    }

    private static void addImports(List<String> imports, RustType rustType) {
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
                imports.add(importPath.toString());
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

    static class Property {
        Property(SnakeCaseName name, RustType type, String serdeType) {
            this.propertyName = name;
            this.rustType = type;
            this.serdeType = serdeType;
        }

        public RustType getRustType() { return this.rustType; }

        // Methods for accessing properties in Mustache context
        public String name() { return this.propertyName.toString(); }
        public String type() { return this.rustType.toString(); }

        SnakeCaseName propertyName;
        RustType rustType;

        // Mustache property
        String serdeType;
    }

    static class Import {
        Import(String path) {
            this.path = path;
        }

        String path;
    }
}
