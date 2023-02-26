package com.twardyece.dmtf.model;

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

        public Variant(RustIdentifier variantName, String serdeName) {
            this.variantName = variantName;
            this.serdeName = serdeName;
        }

        public String name() { return this.variantName.toString(); }
    }
}
