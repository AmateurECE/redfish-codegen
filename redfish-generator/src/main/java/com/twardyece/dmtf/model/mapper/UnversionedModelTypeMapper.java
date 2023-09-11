package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.IdentifierParseError;
import com.twardyece.dmtf.specification.UnversionedSchemaIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnversionedModelTypeMapper implements IModelTypeMapper {
    public UnversionedModelTypeMapper() {}

    @Override
    public Optional<ModelMatchSpecification> matchesType(String name) {
        try {
            UnversionedSchemaIdentifier identifier = new UnversionedSchemaIdentifier(name);
            List<SnakeCaseName> module = new ArrayList<>();
            module.add(new SnakeCaseName(identifier.getModule()));

            return Optional.of(new ModelMatchSpecification(module, identifier.getModel()));
        } catch (IdentifierParseError e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> matchesName(ModelMatchSpecification model) {
        if (model.path().isEmpty()) {
            return Optional.empty();
        }

        PascalCaseName module = new PascalCaseName(model.path().get(model.path().size() - 1));
        return Optional.of(UnversionedSchemaIdentifier.schemaIdentifier(module, model.model()));
    }
}
