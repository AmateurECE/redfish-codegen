package com.twardyece.dmtf.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceMapper {
    private final Pattern namespace;
    private final String replacement;

    public NamespaceMapper(Pattern namespace, String replacement) {
        this.namespace = namespace;
        this.replacement = replacement;
    }

    public boolean matches(String model) {
        Matcher matcher = namespace.matcher(model);
        return matcher.find();
    }

    public String translate(String model) {
        Matcher matcher = namespace.matcher(model);
        return matcher.replaceAll(replacement);
    }
}
