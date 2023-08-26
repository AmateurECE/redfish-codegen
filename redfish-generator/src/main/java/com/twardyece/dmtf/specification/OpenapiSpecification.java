package com.twardyece.dmtf.specification;

import com.twardyece.dmtf.openapi.DocumentParser;
import com.twardyece.dmtf.specification.file.FileList;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OpenapiSpecification {
    private final Path specDirectory;
    private final Map<String, String> inlineSchemaNameMappings;
    private final List<Pattern> ignoredSchemaFiles;
    private static final Pattern SCHEMA_VERSION = Pattern.compile("([0-9]+)_([0-9]+)_([0-9]+)");
    private static final Pattern VERSIONED_SCHEMA_FILE = Pattern.compile("(?<name>[A-Z][A-Za-z]*).v(?<version>" + SCHEMA_VERSION + ").yaml");
    private static final Pattern UNVERSIONED_SCHEMA_PATTERN = Pattern.compile("(?<name>.*).yaml$");
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenapiSpecification.class);

    public OpenapiSpecification(Path specDirectory, Map<String, String> inlineSchemaNameMappings, Pattern[] ignoredSchemaFiles) {
        this.specDirectory = specDirectory;
        this.inlineSchemaNameMappings = inlineSchemaNameMappings;
        List<Pattern> ignoredSchemaFilesList = new ArrayList<>(List.of(ignoredSchemaFiles));
        ignoredSchemaFilesList.add(Pattern.compile("^openapi.yaml$"));
        this.ignoredSchemaFiles = ignoredSchemaFilesList;
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
        List<String> schemaFiles = getSchemaFiles(openapiDirectory);
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

    /**
     * Checks whether this schema file matches any patterns that we were instructed to ignore. If it does not, then this
     * schema file is "applied" to the next stage of specification parsing.
     * @param file A filename to check
     * @return true if we want to apply this schema file
     */
    private boolean isApplicableSchemaFile(String file) {
        for (Pattern pattern : ignoredSchemaFiles) {
            Matcher matcher = pattern.matcher(file);
            if (matcher.find()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param file A filename to check
     * @return true if the filename refers to a versioned schema file
     */
    private static boolean isUnversionedSchema(String file) {
        Matcher matcher = VERSIONED_SCHEMA_FILE.matcher(file);
        return !matcher.find();
    }

    /**
     *
     * @param file The unversioned schema filename to match
     * @param versionedSchemas The list of versioned schemas
     * @return true if `file` does not share the name of a versioned file in versionedSchemas
     */
    private boolean hasNoCorrespondingVersionedSchema(String file, List<VersionedFileDiscovery.VersionedFile> versionedSchemas) {
        Matcher matcher = UNVERSIONED_SCHEMA_PATTERN.matcher(file);
        if (!matcher.find()) {
            return false;
        }

        String name = matcher.group("name");
        Optional<VersionedFileDiscovery.VersionedFile> versionedFile = versionedSchemas
                .stream()
                .filter((f) -> f.name.equals(name))
                .findFirst();
        return versionedFile.isEmpty();
    }

    /**
     * Obtain the list of schema files to parse and apply to the specification. This is not just a listing of the schema
     * directory, there are multiple stages of filtering applied.
     * @param openapiDirectory The path to the directory containing "openapi.yaml"
     * @return A list of the schemas that should be parsed.
     */
    private List<String> getSchemaFiles(Path openapiDirectory) {
        // 1: List the directory and filter out any files that match patterns we were instructed to discard.
        List<String> schemaFiles = Arrays.stream(Objects.requireNonNull(openapiDirectory.toFile().list()))
                .filter(this::isApplicableSchemaFile)
                .toList();

        // 2. For all versioned schemas, filter out all but the most recent version.
        VersionedFileDiscovery versionedFileDiscovery = new VersionedFileDiscovery(new FileList(schemaFiles, openapiDirectory.toString()));
        List<VersionedFileDiscovery.VersionedFile> versionedFiles = versionedFileDiscovery
                .getFiles(VERSIONED_SCHEMA_FILE, "name", "version", SCHEMA_VERSION);

        // 3. For all unversioned schemas, filter out those that have a corresponding versioned schema file.
        Stream<String> unversionedUniqueSchemas = schemaFiles
                .stream()
                .filter(OpenapiSpecification::isUnversionedSchema)
                .filter((f) -> hasNoCorrespondingVersionedSchema(f, versionedFiles));
        return Stream.concat(
                unversionedUniqueSchemas,
                versionedFiles.stream().map((f) -> f.file.getFileName().toString())
        ).toList();
    }
}
