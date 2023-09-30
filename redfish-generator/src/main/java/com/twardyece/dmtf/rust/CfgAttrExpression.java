package com.twardyece.dmtf.rust;

import java.util.ArrayList;
import java.util.List;

public class CfgAttrExpression implements ToRustExpression {
    private String predicate;
    private List<String> attributes;

    public static CfgAttrExpression withEqualityPredicate(String left, String right) {
        CfgAttrExpression result = new CfgAttrExpression();
        result.predicate = left + " = " + right;
        result.attributes = new ArrayList<>();
        return result;
    }

    public CfgAttrExpression attribute(String attribute) {
        this.attributes.add(attribute);
        return this;
    }

    @Override
    public RustExpression toRustExpression() {
        return new RustExpression(
                "#[cfg_attr(" + this.predicate + ", " + String.join(",", this.attributes) + ")]"
        );
    }
}
