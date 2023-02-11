package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.openapi.DocumentParser;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCasedName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

public class ModelFile {
    private SnakeCaseName[] module;
    private Mustache template;
    private SnakeCaseName modelModule;
    private String path;
    private String basePath;
    private ModelContext context;
    final Logger LOGGER = LoggerFactory.getLogger(ModelFile.class);

    public ModelFile(SnakeCaseName[] module, Schema schema, Mustache template, String modelsBasePath) {
        this.module = module;
        this.template = template;

        // Ensure that the model name is PascalCase'd
        schema.setName(CaseConversion.toPascalCase(schema.getName()).toString());

        ArrayList<String> moduleAsString = new ArrayList<>();
        for (SnakeCaseName component : this.module) {
            moduleAsString.add(component.toString());
        }

        this.modelModule = new SnakeCaseName(new PascalCasedName(schema.getName()));
        if ("".equals(modelModule.toString())) {
            LOGGER.warn("modelModule is empty for model " + schema.getName());
        }

        this.path = modelsBasePath + "/" + String.join("/", moduleAsString) + "/" + modelModule.toString()
                + RustConfig.FILE_EXTENSION;
        this.basePath = modelsBasePath;
        this.context = new ModelContext(schema);
    }

    public void registerModel(Map<String, ModuleFile> modules, FileFactory factory) {
        String path = this.basePath;
        for (SnakeCaseName component : this.module) {
            if (!modules.containsKey(path)) {
                modules.put(path, factory.makeModuleFile(path + RustConfig.FILE_EXTENSION));
            }

            modules.get(path).addSubmodule(component);
            path += "/" + component;
        }

        // Can't forget to add this model as a module!
        if (!modules.containsKey(path)) {
            modules.put(path, factory.makeModuleFile(path + RustConfig.FILE_EXTENSION));
        }

        modules.get(path).addSubmodule(this.modelModule);
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
        this.template.execute(writer, this.context);
        writer.flush();
    }
}
