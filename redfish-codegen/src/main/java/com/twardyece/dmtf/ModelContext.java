package com.twardyece.dmtf;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModelContext {
    String name;
    List<Property> properties;
    SnakeCaseName modelModule;

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
        if (null != properties) {
            this.properties = (List<Property>) schema.getProperties().entrySet().stream()
                    .map((s) -> toProperty((Map.Entry<String, Schema>)s, resolver))
                    .collect(Collectors.toList());
        }
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

    private static SnakeCaseName sanitizePropertyName(String name) {
        List<SnakeCaseName> safeName = Arrays.stream(
                replaceInvalidCharacters(
                        removeReservedCharactersInFirstPosition(name))
                .split(" "))
                .map((identifier) -> CaseConversion.toSnakeCase(identifier))
                .collect(Collectors.toList());
        return new SnakeCaseName(safeName);
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
            this.propertyType = type;
            this.serdeType = serdeType;
        }

        public String name() { return this.propertyName.toString(); }
        public String type() { return this.propertyType.toString(); }

        SnakeCaseName propertyName;
        RustType propertyType;
        String serdeType;
    }
}
