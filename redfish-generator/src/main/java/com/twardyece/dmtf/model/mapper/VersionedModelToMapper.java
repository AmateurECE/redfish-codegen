package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.IdentifierParseError;
import com.twardyece.dmtf.specification.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;

public class VersionedModelToMapper implements IModelTypeMapper {
    public VersionedModelToMapper() {}

    @Override
    public ModelMatchResult matches(String name) {
        try {
            VersionedSchemaIdentifier identifier = new VersionedSchemaIdentifier(name);
            List<SnakeCaseName> module = new ArrayList<>();
            module.add(new SnakeCaseName(identifier.getModule()));
            module.add(identifier.getVersion());

            return new ModelMatchResult(module, identifier.getModel());
        } catch (IdentifierParseError e) {
            return null;
        }
    }
}
