package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.IdentifierParseError;
import com.twardyece.dmtf.specification.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VersionedModelTypeMapper implements IModelTypeMapper {
    public VersionedModelTypeMapper() {}

    @Override
    public Optional<ModelMatchSpecification> matchesType(String name) {
        try {
            VersionedSchemaIdentifier identifier = new VersionedSchemaIdentifier(name);
            List<SnakeCaseName> module = new ArrayList<>();
            module.add(new SnakeCaseName(identifier.getModule()));
            module.add(identifier.getVersion());

            return Optional.of(new ModelMatchSpecification(module, identifier.getModel()));
        } catch (IdentifierParseError e) {
            return Optional.empty();
        }
    }
}
