package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.model.context.ModelContext;
import io.swagger.v3.oas.models.media.Schema;

public interface IModelContextFactory {
    ModelContext makeModelContext(RustType type, Schema schema);
}
