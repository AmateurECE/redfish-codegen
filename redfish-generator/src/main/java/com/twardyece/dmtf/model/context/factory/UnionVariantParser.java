package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.rust.RustIdentifier;
import com.twardyece.dmtf.specification.SimpleModelIdentifierFactory;
import com.twardyece.dmtf.specification.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.Optional;

public class UnionVariantParser {
    private final SimpleModelIdentifierFactory[] identifierParsers;

    public UnionVariantParser(SimpleModelIdentifierFactory[] identifierParsers) {
        this.identifierParsers = identifierParsers;
    }

    public RustIdentifier getVariantName(String identifier) {
        Optional<PascalCaseName> identifierName = Optional.empty();
        for (SimpleModelIdentifierFactory parser : identifierParsers) {
            identifierName = parser.modelName(identifier);
            if (identifierName.isPresent()) {
                break;
            }
        }

        RustIdentifier value;
        if (identifierName.isEmpty()) {
            VersionedSchemaIdentifier versioned = new VersionedSchemaIdentifier(identifier);
            value = new RustIdentifier(versioned.getVersion());
        } else {
            value = new RustIdentifier(identifierName.get());
        }

        return value;
    }
}
