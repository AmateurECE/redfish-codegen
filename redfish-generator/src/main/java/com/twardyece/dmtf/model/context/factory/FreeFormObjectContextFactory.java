package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.TupleContext;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.media.Schema;

public class FreeFormObjectContextFactory implements IModelContextFactory {
    private static final RustType jsonValue = new RustType(CratePath.parse("serde_json"), new PascalCaseName("Value"));

    public FreeFormObjectContextFactory() {}

    @Override
    public ModelContext makeModelContext(RustType rustType, Schema schema) {
        String type = schema.getType();
        if (null == type || !"object".equals(type) || null == schema.getProperties() || !schema.getProperties().isEmpty()) {
            return null;
        }

        return ModelContext.forTuple(rustType, new TupleContext(jsonValue), schema.getDescription());
    }
}
