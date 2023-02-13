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
                    .map((s) -> toProperty((Map.Entry<String, Schema>)s))
                    .collect(Collectors.toList());
        }
    }

    public SnakeCaseName getModule() { return this.modelModule; }

    private static Property toProperty(Map.Entry<String, Schema> property) {
        String sanitizedName = sanitizePropertyName(property.getKey());
        String serdeType = null;
        if (!sanitizedName.equals(property.getKey())) {
            serdeType = property.getKey();
        }
        return new Property(sanitizedName, typeFromSchema(property.getValue()), serdeType);
    }

    private static String sanitizePropertyName(String name) {
        List<SnakeCaseName> safeName = Arrays.stream(
                replaceInvalidCharacters(
                        removeReservedCharactersInFirstPosition(name))
                .split(" "))
                .map((identifier) -> CaseConversion.toSnakeCase(identifier))
                .collect(Collectors.toList());
        return new SnakeCaseName(safeName).toString();
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

    private static String typeFromSchema(Schema schema) {
        String type = schema.getType();
        if (null == type) {
            return schema.get$ref();
        } else if ("array".equals(type)) {
            return "Vec<" + typeFromSchema(schema.getItems()) + ">";
        } else {
            if (!RustConfig.RUST_TYPE_MAP.containsKey(type)) {
                LOGGER.warn("No mapping for type " + type);
            }
            return RustConfig.RUST_TYPE_MAP.get(type);
        }
    }

    static class Property {
        Property(String name, String type, String serdeType) {
            this.name = name;
            this.type = type;
            this.serdeType = serdeType;
        }

        String name;
        String type;
        String serdeType;
    }
}
