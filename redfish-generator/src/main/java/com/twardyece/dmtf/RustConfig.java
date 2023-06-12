package com.twardyece.dmtf;

import com.twardyece.dmtf.text.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RustConfig {
    public static final String FILE_EXTENSION = ".rs";
    public static final SnakeCaseName ROUTING_BASE_MODULE = new SnakeCaseName("routing");
    public static final SnakeCaseName MODELS_BASE_MODULE = new SnakeCaseName("models");
    public static final SnakeCaseName REGISTRY_BASE_MODULE = new SnakeCaseName("registries");
    public static final SnakeCaseName CRATE_SOURCE_DIRECTORY = new SnakeCaseName("src");
    public static final SnakeCaseName CRATE_ROOT_MODULE = new SnakeCaseName("crate");
    public static final String CRATE_ROOT_FILE = "lib.rs";
    public static final List<SnakeCaseName> RESERVED_KEYWORDS;

    private static final Pattern reservedCharactersInFirstPosition = Pattern.compile("^@");
    private static final Pattern invalidCharacters = Pattern.compile("[@./:#-]");

    static {
        RESERVED_KEYWORDS = new ArrayList<>();
        RESERVED_KEYWORDS.add(new SnakeCaseName("type"));
        RESERVED_KEYWORDS.add(new SnakeCaseName("ref"));
    }

    public static SnakeCaseName escapeReservedKeyword(SnakeCaseName identifier) {
        if (RESERVED_KEYWORDS.contains(identifier)) {
            return new SnakeCaseName("r#" + identifier.toString());
        } else {
            return identifier;
        }
    }

    public static SnakeCaseName sanitizePropertyName(String name) {
        List<SnakeCaseName> safeName = Arrays.stream(
                        replaceInvalidCharacters(
                                removeReservedCharactersInFirstPosition(name))
                                .split(" "))
                .filter((identifier) -> !"".equals(identifier))
                .map((identifier) -> CaseConversion.toSnakeCase(identifier))
                .collect(Collectors.toList());
        return RustConfig.escapeReservedKeyword(new SnakeCaseName(safeName));
    }

    public static PascalCaseName sanitizeIdentifier(String name) {
        List<PascalCaseName> components = Arrays.stream(
                replaceInvalidCharacters(
                        removeReservedCharactersInFirstPosition(name)
                ).split(" "))
                .filter((identifier) -> !"".equals(identifier))
                .map((identifier) -> CaseConversion.toPascalCase(identifier))
                .collect(Collectors.toList());
        return new PascalCaseName(components);
    }

    private static String removeReservedCharactersInFirstPosition(String name) {
        Matcher matcher = reservedCharactersInFirstPosition.matcher(name);
        if (matcher.find()) {
            return matcher.replaceFirst("");
        } else {
            return name;
        }
    }

    private static String replaceInvalidCharacters(String name) {
        Matcher matcher = invalidCharacters.matcher(name);
        if (matcher.find()) {
            return matcher.replaceAll(" ");
        } else {
            return name;
        }
    }
}
