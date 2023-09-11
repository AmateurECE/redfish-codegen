package com.twardyece.dmtf.specification;

import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedSchemaIdentifier {
    private final PascalCaseName module;
    private final SnakeCaseName version;
    private final PascalCaseName model;

    // This regular expression matches the version string used in versioned schema identifiers
    // in the Redfish Data Model
    private static final Pattern VERSION_PATTERN = Pattern.compile("v[0-9]+_[0-9]+_[0-9]+");

    // This regular expression identifies schemas in the OpenAPI document which are tagged with a version.
    private static final Pattern pattern = Pattern.compile(
            "(?<module>[a-zA-z0-9]*)_(?<version>" + VERSION_PATTERN + ")_(?<model>[a-zA-Z0-9]+)");

    public VersionedSchemaIdentifier(String name) {
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            throw new IdentifierParseError(name + " is not a versioned schema");
        }

        this.module = new PascalCaseName(matcher.group("module"));
        this.version = new SnakeCaseName(matcher.group("version"));
        this.model = new PascalCaseName(matcher.group("model"));
    }

    public PascalCaseName getModule() { return this.module; }
    public SnakeCaseName getVersion() { return this.version; }
    public PascalCaseName getModel() { return this.model; }

    public static String identifier(PascalCaseName module, SnakeCaseName version, PascalCaseName model) {
        return module + "_" + version + "_" + model;
    }

    public static boolean isVersion(String content) {
        Matcher matcher = VERSION_PATTERN.matcher(content);
        return matcher.find();
    }
}
