package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StructContext {
    public List<Property> properties;
    public StructContext(List<Property> properties) {
        this.properties = properties;
    }

    public List<RustType> getDependentTypes() {
        return this.properties.stream()
                .map((p) -> p.rustType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static class Property {
        public Property(SnakeCaseName name, RustType rustType, boolean optional, String serdeName, String docComment) {
            this.propertyName = name;
            this.rustType = rustType;
            this.optional = optional;
            this.serdeName = serdeName;
            this.docComment = docComment;
        }

        // Methods for accessing properties in Mustache context
        public String name() { return this.propertyName.toString(); }
        public String type() {
            if (null == this.typeOverride) {
                return this.rustType.toString();
            } else {
                return this.typeOverride;
            }
        }
        public RustType getRustType() { return this.rustType; }
        public void setTypeOverride(String typeOverride) {
            this.typeOverride = typeOverride;
            this.rustType = null;
        }

        private SnakeCaseName propertyName;
        private RustType rustType;
        private String typeOverride;
        public boolean optional;
        public String serdeName;
        public String docComment;
    }
}
