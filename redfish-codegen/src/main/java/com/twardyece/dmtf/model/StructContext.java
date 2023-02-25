package com.twardyece.dmtf.model;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;

public class StructContext {
    List<Property> properties;
    public StructContext(List<Property> properties) {
        this.properties = properties;
    }

    static class Property {
        Property(SnakeCaseName name, RustType type, String serdeName) {
            this.propertyName = name;
            this.rustType = type;
            this.serdeName = serdeName;
        }

        // Methods for accessing properties in Mustache context
        public String name() { return this.propertyName.toString(); }
        public String type() { return this.rustType.toString(); }

        SnakeCaseName propertyName;
        RustType rustType;

        // Mustache property
        String serdeName;
    }
}
