package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class UnionContextFactory implements IModelContextFactory {
    private ModelResolver modelResolver;
    private final UnionVariantParser variantParser;

    public UnionContextFactory(ModelResolver modelResolver, UnionVariantParser variantParser) {
        this.modelResolver = modelResolver;
        this.variantParser = variantParser;
    }

    @Override
    public ModelContext makeModelContext(RustType type, Schema schema) {
        if (null == schema.getAnyOf()) {
            return null;
        }

        List<EnumContext.Variant> variants = makeVariants(schema);
        return ModelContext.forEnum(type, new EnumContext(variants, false), schema.getDescription());
    }

    private List<EnumContext.Variant> makeVariants(Schema schema) {
        List<EnumContext.Variant> variants = new ArrayList<>();

        for (Object object : schema.getAnyOf()) {
            Schema variant = (Schema)object;
            String identifier = this.modelResolver.getSchemaIdentifier(variant);
            RustIdentifier value = this.variantParser.getVariantName(identifier);
            variants.add(new EnumContext.Variant(value,
                    new EnumContext.Variant.Type(this.modelResolver.resolveSchema(variant)),
                    null, null));
        }

        return variants;
    }
}
