package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.factory.IModelContextFactory;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileFactory {
    private Mustache modelTemplate;
    private Mustache moduleTemplate;
    private IModelContextFactory[] contextFactories;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileFactory.class);

    public FileFactory(MustacheFactory factory, IModelContextFactory[] contextFactories) {
        this.modelTemplate = factory.compile("templates/model.mustache");
        this.moduleTemplate = factory.compile("templates/module.mustache");
        this.contextFactories = contextFactories;
    }

    public ModuleFile<ModelContext> makeModelFile(RustType rustType, Schema schema) {
        for (IModelContextFactory factory : this.contextFactories) {
            ModelContext modelContext = factory.makeModelContext(rustType, schema);
            if (null != modelContext) {
                return new ModuleFile<>(modelContext.moduleContext.path, modelContext, this.modelTemplate);
            }
        }
        LOGGER.error("No ModelContextFactory matching Rust type " + rustType);
        return null;
    }

    public ModuleFile<ModuleContext> makeModuleFile(ModuleContext context) {
        return new ModuleFile<>(context.path, context, this.moduleTemplate);
    }

    // TODO: Add a makeTraitFile method here?
}
