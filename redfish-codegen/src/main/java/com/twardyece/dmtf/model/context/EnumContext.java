package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnumContext {
    List<Variant> variants;
    boolean tagged;

    public EnumContext(List<Variant> variants, boolean tagged) {
        this.variants = variants;
        this.tagged = tagged;
    }


    public List<RustType> getDependentTypes() {
        return this.variants.stream()
                .filter((v) -> null != v.type)
                .map((v) -> v.type.rustType)
                .collect(Collectors.toList());
    }


    public static class Variant {
        RustIdentifier variantName;
        String serdeName;
        String docComment;
        Type type;

        public Variant(RustIdentifier variantName, Type type, String serdeName, String docComment) {
            this.variantName = variantName;
            this.serdeName = serdeName;
            this.docComment = docComment;
            this.type = type;
        }

        public String name() { return this.variantName.toString(); }

        public static class Type {
            RustType rustType;

            public Type(RustType rustType) { this.rustType = rustType; }
            public String type() { return this.rustType.toString(); }
        }
    }
}
