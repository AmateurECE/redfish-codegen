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

        public Variant(PascalCaseName variantName) {
            this.variantName = variantName;
        }

        public String name() { return this.variantName.toString(); }
    }
}
