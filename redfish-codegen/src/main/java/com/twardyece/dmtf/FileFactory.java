package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.twardyece.dmtf.model.ModelContext;
import com.twardyece.dmtf.model.ModelFile;
import com.twardyece.dmtf.model.contextfactory.IModelContextFactory;
import io.swagger.v3.oas.models.media.Schema;

public class FileFactory {
    private MustacheFactory factory;
    private Mustache modelTemplate;
    private Mustache moduleTemplate;
    private IModelContextFactory[] contextFactories;

    public FileFactory(MustacheFactory factory, IModelContextFactory[] contextFactories) {
        this.factory = factory;
        this.modelTemplate = factory.compile("templates/model.mustache");
        this.moduleTemplate = factory.compile("templates/module.mustache");
        this.contextFactories = contextFactories;
    }

    public ModelFile makeModelFile(RustType rustType, Schema schema) {
        for (IModelContextFactory factory : this.contextFactories) {
            ModelContext context = factory.makeModelContext(rustType, schema);
            if (null != context) {
                return new ModelFile(context, this.modelTemplate);
            }
        }
        throw new RuntimeException("No ModelContextFactory matching");
    }

    public ModuleFile makeModuleFile(CratePath path) {
        return new ModuleFile(path, this.moduleTemplate);
    }

    // TODO: Add a makeTraitFile method here?
}
