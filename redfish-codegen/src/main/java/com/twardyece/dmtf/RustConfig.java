package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RustConfig {
    public static final String FILE_EXTENSION = ".rs";
    public static final SnakeCaseName MODELS_BASE_MODULE = new SnakeCaseName("models");
    public static final SnakeCaseName CRATE_SOURCE_DIRECTORY = new SnakeCaseName("src");
    public static final SnakeCaseName CRATE_ROOT_MODULE = new SnakeCaseName("crate");

    public static final String CRATE_ROOT_FILE = "lib.rs";
}
