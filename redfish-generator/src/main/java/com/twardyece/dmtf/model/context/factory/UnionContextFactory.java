package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.rust.RustIdentifier;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class UnionContextFactory implements IModelContextFactory {
    private final ModelResolver modelResolver;
    private final UnionVariantParser variantParser;

    public UnionContextFactory(ModelResolver modelResolver, UnionVariantParser variantParser) {
        this.modelResolver = modelResolver;
        this.variantParser = variantParser;
    }

    @Override
    public ModelContext makeModelContext(RustType type, Schema schema) {
        // Since Redfish 2024.1, anyOf is not used anymore
        // Only one (the latest) of all schemes is declared in the $ref field,
        if (null == schema.getAnyOf() && null == schema.get$ref()) {
            return null;
        }

        List<EnumContext.Variant> variants = makeVariants(schema);
        return ModelContext.forEnum(type, new EnumContext(variants, 0, false), schema.getDescription());
    }

    private List<EnumContext.Variant> makeVariants(Schema schema) {
        List<EnumContext.Variant> variants = new ArrayList<>();

        List<Object> schemataToResolve = new ArrayList<>();

        if (null != schema.getAnyOf()) {
                schemataToResolve.addAll(schema.getAnyOf());
        }

        if (null != schema.get$ref()) {
                schemataToResolve.add(schema);
        }

        for (Object object : schemataToResolve) {
            Schema variant = (Schema)object;
            String identifier = ModelResolver.getSchemaIdentifier(variant.get$ref());
            RustIdentifier value = this.variantParser.getVariantName(identifier);
            variants.add(new EnumContext.Variant(value,
                    new EnumContext.Variant.Type(this.modelResolver.resolveSchema(variant)),
                    null, null));
        }

        return variants;
    }
}
