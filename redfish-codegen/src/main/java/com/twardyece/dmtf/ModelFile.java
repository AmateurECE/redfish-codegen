package com.twardyece.dmtf;

import com.fasterxml.jackson.databind.Module;
import io.swagger.v3.oas.models.media.Schema;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ModelFile {
    Schema schema;
    String[] module;

    public ModelFile(String[] module, Schema schema) {
        this.schema = schema;
        this.module = module;
    }

    public void registerModel(Map<String, ModuleFile> modules) {
        String path = "";
        for (String component : this.module) {
            if (!"".equals(path)) {
                modules.get(path).addSubmodule(component);
            } else {
                path = "src/" + RustConfig.MODELS_BASE_MODULE;
            }

            path += "/" + component;
            if (!modules.containsKey(path)) {
                modules.put(path, new ModuleFile(path + RustConfig.FILE_EXTENSION));
            }
        }
    }

    public void generate() throws IOException {
        String path = "src/" + RustConfig.MODELS_BASE_MODULE + "/" + String.join("/", this.module) + "/"
                + this.schema.getName() + RustConfig.FILE_EXTENSION;
        File modelFile = new File(path);
        File parent = modelFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        modelFile.createNewFile();
    }
}
