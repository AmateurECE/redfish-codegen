package com.twardyece.dmtf;

import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class ModelContext {
    String name;
    List<Property> properties;
    static final Logger LOGGER = LoggerFactory.getLogger(ModelFile.class);

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
        return new Property(property.getKey(), typeFromSchema(property.getValue()));
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
