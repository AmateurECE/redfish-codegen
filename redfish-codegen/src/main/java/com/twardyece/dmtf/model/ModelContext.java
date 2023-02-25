package com.twardyece.dmtf.model;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;

public class ModelContext {
    PascalCaseName modelName;
    SnakeCaseName modelModule;
    List<Property> properties;
    List<Import> imports;

    public ModelContext(PascalCaseName modelName, SnakeCaseName modelModule, List<Property> properties, List<Import> imports) {
        this.modelName = modelName;
        this.modelModule = modelModule;
        this.properties = properties;
        this.imports = imports;
    }

    public String name() { return this.modelName.toString(); }

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

    static class Import {
        Import(CratePath cratePath) {
            this.cratePath = cratePath;
        }

        CratePath cratePath;

        public String path() { return this.cratePath.toString(); }
    }
}
