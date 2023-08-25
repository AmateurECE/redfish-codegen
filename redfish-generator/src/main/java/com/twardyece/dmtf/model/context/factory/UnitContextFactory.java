package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.FileFactory;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.ModelContext;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitContextFactory implements IModelContextFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnitContextFactory.class);

    @Override
    public ModelContext makeModelContext(RustType type, Schema schema) {
        LOGGER.info("Creating unit struct for " + type);
        return ModelContext.forUnit(type, schema.getDescription());
    }
}
