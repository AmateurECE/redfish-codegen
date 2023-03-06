package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.identifiers.IdentifierParseError;
import com.twardyece.dmtf.identifiers.UnversionedSchemaIdentifier;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;

public class UnversionedModelMapper implements IModelFileMapper {
    public UnversionedModelMapper() {}

    @Override
    public ModelMatchResult matches(String name) {
        try {
            UnversionedSchemaIdentifier identifier = new UnversionedSchemaIdentifier(name);
            List<SnakeCaseName> module = new ArrayList<>();
            module.add(new SnakeCaseName(identifier.getModule()));

            return new ModelMatchResult(module, identifier.getModel());
        } catch (IdentifierParseError e) {
            return null;
        }
    }
}
