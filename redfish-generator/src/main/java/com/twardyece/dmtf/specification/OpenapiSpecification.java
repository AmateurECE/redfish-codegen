package com.twardyece.dmtf.specification;

import com.twardyece.dmtf.openapi.DocumentParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenapiSpecification {
    private final Path specDirectory;
    private final Map<String, String> inlineSchemaNameMappings;
    private static final Pattern VERSIONED_SCHEMA_FILE = Pattern.compile("[A-Z][A-Za-z]*.v[0-9]+_[0-9]+_[0-9]+.yaml");
    private static final List<String> ignoredSchemaFiles;
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenapiSpecification.class);

    static {
        ignoredSchemaFiles = new ArrayList<>();
        ignoredSchemaFiles.add("redfish-payload-annotations-v1.yaml");
    }

    public OpenapiSpecification(Path specDirectory, Map<String, String> inlineSchemaNameMappings) {
        this.specDirectory = specDirectory;
        this.inlineSchemaNameMappings = inlineSchemaNameMappings;
    }

    private static void debugInformDuplicateSchemas(String schema, String file) {
        if (schema.endsWith("_1")) {
            LOGGER.warn("Duplicate schema " + schema + " encountered while parsing " + file);
        }
    }

    public OpenAPI getRedfishDataModel() {
        Path openapiDirectory = Path.of(this.specDirectory + "/openapi");
        DocumentParser documentParser = new DocumentParser(openapiDirectory + "/openapi.yaml");
        this.inlineSchemaNameMappings.forEach(documentParser::addInlineSchemaNameMapping);
        OpenAPI redfishDataModel = documentParser.parse();

        Components redfishComponents = redfishDataModel.getComponents();
        List<String> schemaFiles = Arrays.stream(Objects.requireNonNull(openapiDirectory.toFile().list()))
                .filter(OpenapiSpecification::isGenerationCandidate)
                .toList();
        for (String file : schemaFiles) {
            DocumentParser schemaParser = new DocumentParser(openapiDirectory + "/" + file);
            this.inlineSchemaNameMappings.forEach(schemaParser::addInlineSchemaNameMapping);
            OpenAPI schemaDocument = schemaParser.parse();
            if (null == schemaDocument.getComponents().getSchemas()) {
                continue;
            }

            for (Map.Entry<String, Schema> entry : schemaDocument.getComponents().getSchemas().entrySet()) {
                debugInformDuplicateSchemas(entry.getKey(), file);
                if (!redfishComponents.getSchemas().containsKey(entry.getKey())) {
                    redfishComponents.addSchemas(entry.getKey(), entry.getValue());
                }
            }
        }

        return redfishDataModel;
    }

    private static boolean isGenerationCandidate(String file) {
        Matcher matcher = VERSIONED_SCHEMA_FILE.matcher(file);
        boolean isUnversionedSchema = !matcher.find();
        return isUnversionedSchema && !ignoredSchemaFiles.contains(file);
    }
}
