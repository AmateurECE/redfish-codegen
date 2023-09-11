package com.twardyece.dmtf.specification;

import com.twardyece.dmtf.text.PascalCaseName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnversionedSchemaIdentifier {
    private final PascalCaseName module;
    private final PascalCaseName model;

    // This regular expression identifies models in the OpenAPI document which are not tagged with a version.
    private static final Pattern pattern = Pattern.compile("(?<module>[a-zA-z0-9]*)_(?<model>[a-zA-Z0-9]+)");

    public UnversionedSchemaIdentifier(String name) {
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            throw new IdentifierParseError(name + " is not an unversioned identifier");
        }

        this.module = new PascalCaseName(matcher.group("module"));
        this.model = new PascalCaseName(matcher.group("model"));
    }

    public PascalCaseName getModule() { return this.module; }
    public PascalCaseName getModel() { return this.model; }

    public static String schemaIdentifier(PascalCaseName module, PascalCaseName model) {
        return module + "_" + model;
    }
}
