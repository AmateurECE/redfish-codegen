package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.api.NameMapper;
import com.twardyece.dmtf.identifiers.VersionedSchemaIdentifier;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UnionContextFactory implements IModelContextFactory {
    private ModelResolver modelResolver;

    public UnionContextFactory(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    @Override
    public ModelContext makeModelContext(RustType type, Schema schema) {
        if (null == schema.getAnyOf()) {
            return null;
        }

        List<EnumContext.Variant> variants = makeVariants(schema);
        return ModelContext.forEnum(type, new EnumContext(variants, false), null, schema.getDescription());
    }

    private List<EnumContext.Variant> makeVariants(Schema schema) {
        List<EnumContext.Variant> variants = new ArrayList<>();

        // TODO: We should probably delegate this parsing to an injected dependency in the future, since it's possible
        //  this will change in future revisions of the Redfish data model.
        NameMapper mapper = new NameMapper(Pattern.compile("odata-v4_(?<model>[a-zA-Z0-9]*)"), "model");

        for (Object object : schema.getAnyOf()) {
            Schema variant = (Schema)object;
            String identifier = this.modelResolver.getSchemaIdentifier(variant);
            SnakeCaseName identifierName = mapper.matchComponent(identifier);
            RustIdentifier value;
            if (null == identifierName) {
                VersionedSchemaIdentifier versioned = new VersionedSchemaIdentifier(identifier);
                value = new RustIdentifier(versioned.getVersion());
            } else {
                value = new RustIdentifier(new PascalCaseName(identifierName));
            }

            variants.add(new EnumContext.Variant(value, new EnumContext.Variant.Type(this.modelResolver.resolveSchema(variant)),
                    null, null));
        }

        return variants;
    }
}
