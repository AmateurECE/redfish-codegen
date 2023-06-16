package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.model.NameMapper;
import com.twardyece.dmtf.identifiers.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.regex.Pattern;

public class UnionVariantParser {
    private static final NameMapper mapper = new NameMapper(Pattern.compile("odata-v4_(?<model>[a-zA-Z0-9]*)"), "model");

    public UnionVariantParser() {}

    public RustIdentifier getVariantName(String identifier) {
        SnakeCaseName identifierName = mapper.matchComponent(identifier);
        RustIdentifier value;
        if (null == identifierName) {
            VersionedSchemaIdentifier versioned = new VersionedSchemaIdentifier(identifier);
            value = new RustIdentifier(versioned.getVersion());
        } else {
            value = new RustIdentifier(new PascalCaseName(identifierName));
        }

        return value;
    }
}
