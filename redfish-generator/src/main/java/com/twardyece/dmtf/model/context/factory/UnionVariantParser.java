package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.rust.RustIdentifier;
import com.twardyece.dmtf.model.NameMapper;
import com.twardyece.dmtf.specification.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

public class UnionVariantParser {
    private final NameMapper[] nameMappers;

    public UnionVariantParser(NameMapper[] nameMappers) {
        this.nameMappers = nameMappers;
    }

    public RustIdentifier getVariantName(String identifier) {
        SnakeCaseName identifierName = null;
        for (NameMapper nameMapper : nameMappers) {
            identifierName = nameMapper.matchComponent(identifier);
            if (null != identifierName) {
                break;
            }
        }

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
