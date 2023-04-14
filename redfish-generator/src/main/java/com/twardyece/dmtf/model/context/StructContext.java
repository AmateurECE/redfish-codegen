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
        public Property(SnakeCaseName name, RustType rustType, String openapiType, boolean optional, String serdeName, String docComment) {
            this.propertyName = name;
            this.rustType = rustType;
            this.openapiType = openapiType;
            this.optional = optional;
            this.serdeName = serdeName;
            this.docComment = docComment;
            this.skipDeserializing = false;
            this.defaultValue = null;
        }

        // Methods for accessing properties in Mustache context
        public String name() { return this.propertyName.toString(); }
        public String type() {
            return this.rustType.toString();
        }
        public RustType getRustType() { return this.rustType; }
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
        public String getOpenapiType() { return this.openapiType; }
        public void setIsDeserialized(boolean deserialize) {
            this.skipDeserializing = !deserialize;
        }

        // The name of the property in the parent struct
        private SnakeCaseName propertyName;
        // The rust type corresponding to the
        private RustType rustType;
        public final String openapiType;
        public boolean optional;
        public boolean skipDeserializing;
        public String serdeName;
        public String docComment;
        public String defaultValue;
    }
}
