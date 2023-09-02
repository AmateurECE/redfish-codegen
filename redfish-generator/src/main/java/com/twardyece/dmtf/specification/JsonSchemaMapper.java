package com.twardyece.dmtf.specification;

import java.util.Optional;

/**
 * Associate a json-schema file with OpenAPI paths matching a provided specification.
 */
public class JsonSchemaMapper {
    private final SimpleModelIdentifierFactory identifierFactory;
    private final String jsonSchema;

    public JsonSchemaMapper(SimpleModelIdentifierFactory identifierFactory, String jsonSchema) {
        this.identifierFactory = identifierFactory;
        this.jsonSchema = jsonSchema;
    }

    public Optional<String> matchJsonSchema(String openapiPath) {
        return this.identifierFactory.modelName(openapiPath)
                .map((unused) -> this.jsonSchema);
    }
}
