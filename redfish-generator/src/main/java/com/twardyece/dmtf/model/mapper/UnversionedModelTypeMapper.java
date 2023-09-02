package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.IdentifierParseError;
import com.twardyece.dmtf.specification.UnversionedSchemaIdentifier;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnversionedModelTypeMapper implements IModelTypeMapper {
    public UnversionedModelTypeMapper() {}

    @Override
    public Optional<ModelMatchResult> matches(String name) {
        try {
            UnversionedSchemaIdentifier identifier = new UnversionedSchemaIdentifier(name);
            List<SnakeCaseName> module = new ArrayList<>();
            module.add(new SnakeCaseName(identifier.getModule()));

            return Optional.of(new ModelMatchResult(module, identifier.getModel()));
        } catch (IdentifierParseError e) {
            return Optional.empty();
        }
    }
}
