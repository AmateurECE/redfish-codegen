package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.twardyece.dmtf.api.TraitContext;
import com.twardyece.dmtf.csdl.MetadataRoutingContext;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.factory.IModelContextFactory;
import com.twardyece.dmtf.registry.RegistryContext;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileFactory {
    private final Mustache modelTemplate;
    private final Mustache moduleTemplate;
    private final Mustache traitTemplate;
    private final Mustache libTemplate;
    private final Mustache registryTemplate;
    private final Mustache metadataTemplate;
    private final IModelContextFactory[] contextFactories;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileFactory.class);

    public FileFactory(MustacheFactory factory, IModelContextFactory[] contextFactories) {
        this.modelTemplate = factory.compile("templates/model.mustache");
        this.moduleTemplate = factory.compile("templates/module.mustache");
        this.traitTemplate = factory.compile("templates/api.mustache");
        this.libTemplate = factory.compile("templates/lib.mustache");
        this.registryTemplate = factory.compile("templates/registry.mustache");
        this.metadataTemplate = factory.compile("templates/metadata.mustache");
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

    public ModuleFile<TraitContext> makeTraitFile(TraitContext trait) {
        return new ModuleFile<>(trait.moduleContext.path, trait, this.traitTemplate);
    }

    public ModuleFile<LibContext> makeLibFile(String specVersion) {
        ModuleContext module = new ModuleContext(CratePath.crateRoot(), null);
        LibContext context = new LibContext(module, specVersion);
        return new ModuleFile<>(context.moduleContext.path, context, this.libTemplate);
    }

    public ModuleFile<RegistryContext> makeRegistryFile(RegistryContext context) {
        return new ModuleFile<>(context.moduleContext.path, context, this.registryTemplate);
    }

    public ModuleFile<MetadataRoutingContext> makeMetadataRoutingFile(MetadataRoutingContext context) {
        return new ModuleFile<>(context.module().path, context, this.metadataTemplate);
    }
}
