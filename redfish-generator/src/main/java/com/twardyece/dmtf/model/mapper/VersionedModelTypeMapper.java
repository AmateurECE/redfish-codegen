package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.IdentifierParseError;
import com.twardyece.dmtf.specification.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.PascalCaseName;
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

    @Override
    public Optional<String> matchesName(ModelMatchSpecification model) {
        List<SnakeCaseName> path = model.path();
        int pathSize = path.size();
        if (pathSize < 2) {
            return Optional.empty();
        }

        SnakeCaseName version = path.get(pathSize - 2);
        if (!VersionedSchemaIdentifier.isVersion(version.toString())) {
            return Optional.empty();
        }

        SnakeCaseName namespace = path.get(pathSize - 1);
        return Optional.of(VersionedSchemaIdentifier.identifier(new PascalCaseName(namespace), version, model.model()));
    }
}
