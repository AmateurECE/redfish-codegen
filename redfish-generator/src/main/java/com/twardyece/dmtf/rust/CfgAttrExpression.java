package com.twardyece.dmtf.rust;

import java.util.ArrayList;
import java.util.List;

public class CfgAttrExpression implements IRustExpression {
    private final String predicate;
    private final List<String> attributes;

    private CfgAttrExpression(String predicate, List<String> attributes) {
        this.predicate = predicate;
        this.attributes = attributes;
    }

    @Override
    public String expression() {
        return "#[cfg_attr(" + this.predicate + ", " + String.join(",", this.attributes) + ")]";
    }

    public static class Builder {
        private String predicate;
        private List<String> attributes;

        public static Builder withEqualityPredicate(String left, String right) {
            Builder result = new Builder();
            result.predicate = left + " = " + right;
            result.attributes = new ArrayList<>();
            return result;
        }

        public Builder attribute(String attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public CfgAttrExpression build() {
            return new CfgAttrExpression(this.predicate, this.attributes);
        }
    }
}
