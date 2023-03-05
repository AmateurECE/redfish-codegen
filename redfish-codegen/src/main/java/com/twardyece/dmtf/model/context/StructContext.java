package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;
import java.util.stream.Collectors;

public class StructContext {
    List<Property> properties;
    public StructContext(List<Property> properties) {
        this.properties = properties;
    }

    public List<RustType> getDependentTypes() {
        return this.properties.stream().map((p) -> p.rustType).collect(Collectors.toList());
    }

    public static class Property {
        public Property(SnakeCaseName name, RustType type, String serdeName, String docComment) {
            this.propertyName = name;
            this.rustType = type;
            this.serdeName = serdeName;
            this.docComment = docComment;
        }

        // Methods for accessing properties in Mustache context
        public String name() { return this.propertyName.toString(); }
        public String type() { return this.rustType.toString(); }

        SnakeCaseName propertyName;
        public RustType rustType;
        String serdeName;
        String docComment;
    }
}
