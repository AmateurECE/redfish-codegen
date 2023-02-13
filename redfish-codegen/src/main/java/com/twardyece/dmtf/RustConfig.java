package com.twardyece.dmtf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RustConfig {
    public static final String FILE_EXTENSION = ".rs";
    public static final String MODELS_BASE_MODULE = "models";

    public static final Map<String, String> RUST_TYPE_MAP;

    static {
        HashMap<String, String> typeMap = new HashMap<>();
        typeMap.put("integer", "i64");
        typeMap.put("string", "String");
        typeMap.put("boolean", "bool");
        typeMap.put("number", "f64");
        RUST_TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }
}
