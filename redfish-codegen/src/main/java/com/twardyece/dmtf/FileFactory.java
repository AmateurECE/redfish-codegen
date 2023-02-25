package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.twardyece.dmtf.model.ModelContext;
import com.twardyece.dmtf.model.ModelContextFactory;
import com.twardyece.dmtf.model.ModelFile;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.media.Schema;

public class FileFactory {
    private MustacheFactory factory;
    private Mustache modelTemplate;
    private Mustache moduleTemplate;
    private ModelContextFactory contextFactory;

    public FileFactory(MustacheFactory factory, ModelContextFactory contextFactory) {
        this.factory = factory;
        this.modelTemplate = factory.compile("templates/model.mustache");
        this.moduleTemplate = factory.compile("templates/module.mustache");
        this.contextFactory = contextFactory;
    }

    public ModelFile makeModelFile(CratePath module, PascalCaseName name, Schema schema) {
        ModelContext context = this.contextFactory.makeModelContext(name, schema);
        return new ModelFile(module, context, this.modelTemplate);
    }

    public ModuleFile makeModuleFile(CratePath path) {
        return new ModuleFile(path, this.moduleTemplate);
    }

    // TODO: Add a makeTraitFile method here?
}
