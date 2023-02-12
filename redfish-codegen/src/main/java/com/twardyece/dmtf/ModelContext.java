package com.twardyece.dmtf;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCasedName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModelContext {
    String name;
    List<Property> properties;
    static final Logger LOGGER = LoggerFactory.getLogger(ModelFile.class);
    static final Pattern reservedCharactersInFirstPosition = Pattern.compile("^@");
    static final Pattern invalidCharacters = Pattern.compile("[@.]");

    public ModelContext(Schema schema) {
        this.name = schema.getName();
        Map<String, Schema> properties = schema.getProperties();
        if (null != properties) {
            this.properties = (List<Property>) schema.getProperties().entrySet().stream()
                    .map((s) -> toProperty((Map.Entry<String, Schema>)s))
                    .collect(Collectors.toList());
        }
    }

    private static Property toProperty(Map.Entry<String, Schema> property) {
        return new Property(sanitizePropertyName(property.getKey()), typeFromSchema(property.getValue()));
    }

    private static String sanitizePropertyName(String name) {
        String safeName = replaceInvalidCharacters(removeReservedCharactersInFirstPosition(name));
        SnakeCaseName postCasedName = null;
        try {
            PascalCasedName preCasedName = new PascalCasedName(safeName);
            postCasedName = new SnakeCaseName(preCasedName);
        } catch (ICaseConvertible.CaseConversionError e) {
            postCasedName = new SnakeCaseName(safeName);
        }
        return postCasedName.toString();
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
            return matcher.replaceAll("_");
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
        Property(String name, String type) {
            this.name = name;
            this.type = type;
        }

        String name;
        String type;
    }
}
