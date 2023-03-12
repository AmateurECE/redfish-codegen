package com.twardyece.dmtf.registry;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.List;

public class RegistryContext {
    public ModuleContext moduleContext;
    public RustType rustType;
    public String docComment;
    public RustType messageRustType;
    public List<Variant> variants;

    public RegistryContext(ModuleContext moduleContext, RustType rustType, String docComment, RustType messageRustType,
                           List<Variant> variants) {
        this.moduleContext = moduleContext;
        this.rustType = rustType;
        this.docComment = docComment;
        this.messageRustType = messageRustType;
        this.variants = variants;
    }

    public String name() { return this.rustType.getName().toString(); }
    public String messageType() { return this.messageRustType.toString(); }

    public static class Variant {
        public PascalCaseName variantName;
        public String docComment;
        public String message;
        public String id;
        public RustIdentifier severityIdentifier;
        public String resolution;
        public List<Field> fields;

        public Variant(PascalCaseName variantName, String docComment, String message, String id,
                       RustIdentifier severityIdentifier, String resolution, List<Field> fields) {
            this.variantName = variantName;
            this.docComment = docComment;
            this.message = message;
            this.id = id;
            this.severityIdentifier = severityIdentifier;
            this.resolution = resolution;
            this.fields = fields;
        }

        public String name() { return this.variantName.toString(); }
        public boolean hasFields() { return null != this.fields && !this.fields.isEmpty(); }
        public String severity() { return this.severityIdentifier.toString(); }

        public static class Field {
            public RustType rustType;
            public String docComment;
            public Field(RustType rustType, String docComment) {
                this.rustType = rustType;
                this.docComment = docComment;
            }
            public String name() { return this.rustType.toString(); }
        }
    }
}
