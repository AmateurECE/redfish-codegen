package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.*;

public class RustConfig {
    public static final String FILE_EXTENSION = ".rs";
    public static final SnakeCaseName MODELS_BASE_MODULE = new SnakeCaseName("models");
    public static final SnakeCaseName CRATE_SOURCE_DIRECTORY = new SnakeCaseName("src");
    public static final SnakeCaseName CRATE_ROOT_MODULE = new SnakeCaseName("crate");
    public static final String CRATE_ROOT_FILE = "lib.rs";
    public static final List<SnakeCaseName> RESERVED_KEYWORDS;

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
}
