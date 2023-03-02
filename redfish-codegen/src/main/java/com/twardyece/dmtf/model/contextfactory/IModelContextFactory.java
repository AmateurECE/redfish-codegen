package com.twardyece.dmtf.model.contextfactory;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelContext;
import io.swagger.v3.oas.models.media.Schema;

public interface IModelContextFactory {
    ModelContext makeModelContext(RustType type, Schema schema);
}
