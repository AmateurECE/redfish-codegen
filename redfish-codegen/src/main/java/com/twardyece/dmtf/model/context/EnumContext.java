package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.List;

public class EnumContext {
    List<Variant> variants;
    boolean tagged;

    public EnumContext(List<Variant> variants, boolean tagged) {
        this.variants = variants;
        this.tagged = tagged;
    }

    public static class Variant {
        RustIdentifier variantName;
        String serdeName;
        String docComment;

        public Variant(RustIdentifier variantName, String serdeName, String docComment) {
            this.variantName = variantName;
            this.serdeName = serdeName;
            this.docComment = docComment;
        }

        public String name() { return this.variantName.toString(); }
    }
}
