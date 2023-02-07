package com.twardyece.dmtf;

import com.fasterxml.jackson.databind.Module;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.swagger.v3.oas.models.media.Schema;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

public class ModelFile {
    private Schema schema;
    private SnakeCaseName[] module;
    private Mustache template;
    private String path;
    private String basePath;

    public ModelFile(SnakeCaseName[] module, Schema schema, Mustache template, String modelsBasePath) {
        this.schema = schema;
        this.module = module;
        this.template = template;

        // Ensure that the model name is PascalCase'd
        schema.setName(CaseConversion.toPascalCase(schema.getName()).toString());

        ArrayList<String> moduleAsString = new ArrayList<>();
        for (SnakeCaseName component : this.module) {
            moduleAsString.add(component.toString());
        }

        SnakeCaseName modelName = new SnakeCaseName(new PascalCasedName(this.schema.getName()));
        if ("".equals(modelName.toString())) {
            System.out.println("[WARN] modelName is empty for model " + this.schema.getName());
        }

        this.path = modelsBasePath + "/" + String.join("/", moduleAsString) + "/" + modelName.toString()
                + RustConfig.FILE_EXTENSION;
        this.basePath = modelsBasePath;
    }

    public void registerModel(Map<String, ModuleFile> modules, FileFactory factory) {
        String path = "";
        for (SnakeCaseName component : this.module) {
            if (!"".equals(path)) {
                modules.get(path).addSubmodule(component);
            } else {
                path = this.basePath;
            }

            path += "/" + component;
            if (!modules.containsKey(path)) {
                modules.put(path, factory.makeModuleFile(path + RustConfig.FILE_EXTENSION));
            }
        }
    }

    public void generate() throws IOException {
        File modelFile = new File(this.path);
        File parent = modelFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        modelFile.createNewFile();

        // Render the template
        Writer writer = new PrintWriter(modelFile);
        // TODO: Convert schema to ModelContext
        this.template.execute(writer, this);
        writer.flush();
    }
}
