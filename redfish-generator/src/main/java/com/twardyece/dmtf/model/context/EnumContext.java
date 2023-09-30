package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.rust.RustIdentifier;
import com.twardyece.dmtf.rust.RustType;

import java.util.List;

public class EnumContext {
    public List<Variant> variants;
    int defaultVariantIndex;
    boolean tagged;

    public EnumContext(List<Variant> variants, int defaultVariantIndex, boolean tagged) {
        this.variants = variants;
        this.defaultVariantIndex = defaultVariantIndex;
        this.tagged = tagged;
    }

    public Variant defaultVariant() { return this.variants.get(this.defaultVariantIndex); }

    public static class Variant {
        RustIdentifier variantName;
        String serdeName;
        String docComment;
        public Type type;

        public Variant(RustIdentifier variantName, Type type, String serdeName, String docComment) {
            this.variantName = variantName;
            this.serdeName = serdeName;
            this.docComment = docComment;
            this.type = type;
        }

        public String name() { return this.variantName.toString(); }

        public static class Type {
            public RustType rustType;

            public Type(RustType rustType) { this.rustType = rustType; }
            public String type() { return this.rustType.toString(); }
        }
    }
}
