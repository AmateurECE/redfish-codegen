package com.twardyece.dmtf;

import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redfish Code Generator for the Rust language
 * Based on swagger-parser v3
 */
public class RedfishCodegen {
    private String apiDirectory;
    private String crateDirectory;

    private OpenAPI document;

    RedfishCodegen(String apiDirectory, String crateDirectory) {
        this.apiDirectory = apiDirectory;
        this.crateDirectory = crateDirectory;


        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
//        parseOptions.setResolveFully(true);
//        parseOptions.setLegacyYamlDeserialization(true);
        this.document = new OpenAPIV3Parser().read(this.apiDirectory + "/openapi.yaml", null, parseOptions);
        if (null == this.document) {
            throw new RuntimeException("Couldn't parse " + this.apiDirectory + "/openapi.yaml");
        }
    }

    private void generateModel(Schema schema, String module) throws IOException {
        // TODO: Render the template!
        File modelFile = new File(this.crateDirectory + "/src/models/" + module + "/" + schema.getName() + ".rs");
        modelFile.createNewFile();
    }

    private void generateModelModuleFile(String moduleName, Set<String> versions) throws IOException {
        // TODO: Render the template!
        File moduleFile = new File(this.crateDirectory + "/src/models/" + moduleName + ".rs");
        moduleFile.createNewFile();
    }

    private void generateModelVersionFile(String version, ArrayList<String> models, String module) throws IOException {
        // TODO: Render the template!
        File versionFile = new File(this.crateDirectory + "/src/models" + module + "/" + version + ".rs");
        versionFile.createNewFile();
    }

    public void generateModels() throws IOException {
        HashMap<String, HashMap<String, ArrayList<String>>> modules = new HashMap<>();
        for (Map.Entry<String, Schema> schema : this.document.getComponents().getSchemas().entrySet()) {
            // The redfish document consistently names models of the form Module_vXX_XX_XX_Model
            Pattern pattern = Pattern.compile("(?<module>[a-zA-z0-9]*)_(?<version>v[0-9]+_[0-9]+_[0-9]+)_(?<model>[a-zA-Z0-9]+)");
            Matcher matcher = pattern.matcher(schema.getKey());
            if (!matcher.find()) {
                throw new InvalidParameterException("Schema name " + schema.getKey() + " does not match expected format!");
            }

            // The actual name of the model is the suffix
            schema.getValue().setName(matcher.group("model"));

            // Add the model name and version to the collection for this module.
            ((ArrayList<String>) modules.getOrDefault(matcher.group("module"), new HashMap())
                    .getOrDefault(matcher.group("version"), new ArrayList())).add(matcher.group("model"));

            generateModel(schema.getValue(), matcher.group("module") + "/" + matcher.group("version"));
        }

        for (Map.Entry<String, HashMap<String, ArrayList<String>>> module : modules.entrySet()) {
            generateModelModuleFile(module.getKey(), module.getValue().keySet());
            for (Map.Entry<String, ArrayList<String>> versions : module.getValue().entrySet()) {
                generateModelVersionFile(versions.getKey(), versions.getValue(), module.getKey());
            }
        }
    }

    public void generateApis() {
        // TODO
    }

    public static void main(String[] args) {
        Option apiDirectoryOption = new Option("apiDirectory", true, "Directory containing openapi resource files");
        apiDirectoryOption.setRequired(true);
        Option crateDirectoryOption = new Option("crateDirectory", true, "Directory containing Cargo.toml, into which output sources are written");
        crateDirectoryOption.setRequired(true);

        Options options = new Options();
        options.addOption(apiDirectoryOption);
        options.addOption(crateDirectoryOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine command = parser.parse(options, args);

            String apiDirectory = command.getOptionValue("apiDirectory");
            String crateDirectory = command.getOptionValue("crateDirectory");

            RedfishCodegen codegen = new RedfishCodegen(apiDirectory, crateDirectory);
            codegen.generateModels();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("RedfishCodegen", options);
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
