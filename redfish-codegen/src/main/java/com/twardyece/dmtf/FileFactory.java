package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

public class FileFactory {
    private MustacheFactory factory;
    private Mustache modelTemplate;
    private Mustache moduleTemplate;
    private String modelsBasePath;
    private ModelResolver resolver;

    public FileFactory(MustacheFactory factory, SnakeCaseName modelsModule, ModelResolver resolver) {
        this.factory = factory;
        this.modelTemplate = factory.compile("templates/model.mustache");
        this.moduleTemplate = factory.compile("templates/module.mustache");
        this.modelsBasePath = "src/" + modelsModule.toString();
        this.resolver = resolver;
    }

    // TODO: Make module a List<>
    public ModelFile makeModelFile(SnakeCaseName[] module, PascalCaseName name, Schema schema) {
        ModelContext context = new ModelContext(name, schema, this.resolver);
        // TODO: Make modelsBasePath a CratePath here, and add method to CratePath to convert to Path.
        return new ModelFile(module, context, this.modelTemplate, modelsBasePath);
    }

    public ModuleFile makeModuleFile(String path) {
        return new ModuleFile(path, this.moduleTemplate);
    }
}
