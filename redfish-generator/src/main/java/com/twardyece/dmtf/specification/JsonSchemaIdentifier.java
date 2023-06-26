package com.twardyece.dmtf.specification;

import java.util.Optional;

public class JsonSchemaIdentifier {
    private final JsonSchemaMapper[] schemaMappers;

    public JsonSchemaIdentifier(JsonSchemaMapper[] schemaMappers) {
        this.schemaMappers = schemaMappers;
    }

    public Optional<String> identify(String identifier) {
        for (JsonSchemaMapper schemaMapper : this.schemaMappers) {
            Optional<String> jsonSchema = schemaMapper.matchJsonSchema(identifier);
            if (jsonSchema.isPresent()) {
                return jsonSchema;
            }
        }

        try {
            VersionedSchemaIdentifier versioned = new VersionedSchemaIdentifier(identifier);
            return Optional.of(versioned.getModule() + "." + versioned.getVersion() + ".json");
        } catch (IdentifierParseError e) {
            UnversionedSchemaIdentifier unversioned = new UnversionedSchemaIdentifier(identifier);
            return Optional.of(unversioned.getModule() + ".json");
        }
    }
}
