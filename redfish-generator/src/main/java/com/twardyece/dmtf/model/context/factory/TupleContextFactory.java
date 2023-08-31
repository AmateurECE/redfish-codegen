package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.TupleContext;
import io.swagger.v3.oas.models.media.Schema;

public class TupleContextFactory implements IModelContextFactory {
    private ModelResolver modelResolver;

    public TupleContextFactory(ModelResolver modelResolver) { this.modelResolver = modelResolver; }

    @Override
    public ModelContext makeModelContext(RustType type, Schema schema) {
        String typeName = schema.getType();
        if (null == typeName) {
            return null;
        }

        TupleContext tupleContext = new TupleContext(this.modelResolver.resolveSchema(schema));
        return ModelContext.forTuple(type, tupleContext, schema.getDescription());
    }
}
