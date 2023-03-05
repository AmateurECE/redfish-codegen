package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.TupleContext;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class FreeFormObjectContextFactory implements IModelContextFactory {
    private static final RustType jsonValue;

    static {
        List<SnakeCaseName> components = new ArrayList<>();
        components.add(new SnakeCaseName("serde_json"));
        CratePath path = CratePath.relative(components);
        jsonValue = new RustType(path, new PascalCaseName("Value"));
    }

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
