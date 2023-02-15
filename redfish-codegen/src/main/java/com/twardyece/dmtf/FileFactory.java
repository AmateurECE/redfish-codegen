package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.twardyece.dmtf.model.ModelContext;
import com.twardyece.dmtf.model.ModelFile;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.media.Schema;

public class FileFactory {
    private MustacheFactory factory;
    private Mustache modelTemplate;
    private Mustache moduleTemplate;
    private ModelResolver resolver;

    public FileFactory(MustacheFactory factory, ModelResolver resolver) {
        this.factory = factory;
        this.modelTemplate = factory.compile("templates/model.mustache");
        this.moduleTemplate = factory.compile("templates/module.mustache");
        this.resolver = resolver;
    }

    public ModelFile makeModelFile(CratePath module, PascalCaseName name, Schema schema) {
        ModelContext context = new ModelContext(name, schema, this.resolver);
        return new ModelFile(module, context, this.modelTemplate);
    }

    // TODO: makeModuleFile should also take a CratePath
    public ModuleFile makeModuleFile(CratePath path) {
        return new ModuleFile(path, this.moduleTemplate);
    }
}
