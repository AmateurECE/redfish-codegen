package com.twardyece.dmtf;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class ModelContext {

    String name;
    List<Property> properties;
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
        Schema schema = property.getValue();
        String type = "";
        if ("object".equals(schema.getType())) {
            type = schema.get$ref();
        } else if ("array".equals(schema.getType())) {
            type = "Vec<" + schema.get$ref() + ">";
        } else {
            type = RustConfig.RUST_TYPE_MAP.get(schema.getType());
        }
        return new Property(property.getKey(), type);
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
