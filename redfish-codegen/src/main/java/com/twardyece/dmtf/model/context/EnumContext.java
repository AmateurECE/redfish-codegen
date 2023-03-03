package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.List;

public class EnumContext {
    List<Variant> variants;

    public EnumContext(List<Variant> variants) {
        this.variants = variants;
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
