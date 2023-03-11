package com.twardyece.dmtf.registry;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.List;

public class RegistryContext {
    public ModuleContext moduleContext;
    public RustType rustType;
    public String docComment;
    public List<Variant> variants;

    public RegistryContext(ModuleContext moduleContext, RustType rustType, String docComment, List<Variant> variants) {
        this.moduleContext = moduleContext;
        this.rustType = rustType;
        this.docComment = docComment;
        this.variants = variants;
    }

    public String name() { return this.rustType.getName().toString(); }

    public static class Variant {
        public PascalCaseName variantName;
        public String docComment;
        public List<Type> types;

        public Variant(PascalCaseName variantName, String docComment, List<Type> types) {
            this.variantName = variantName;
            this.docComment = docComment;
            this.types = types;
        }

        public String name() { return this.variantName.toString(); }
        public boolean hasTypes() { return null != this.types && !this.types.isEmpty(); }

        public static class Type {
            public RustType rustType;
            public String docComment;
            public Type(RustType rustType, String docComment) {
                this.rustType = rustType;
                this.docComment = docComment;
            }
            public String name() { return this.rustType.toString(); }
        }
    }
}
