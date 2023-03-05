package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.identifiers.IdentifierParseError;
import com.twardyece.dmtf.identifiers.VersionedSchemaIdentifier;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedModelMapper implements IModelFileMapper {
    public VersionedModelMapper() {}

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
