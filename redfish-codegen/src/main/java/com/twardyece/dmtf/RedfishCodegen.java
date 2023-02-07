package com.twardyece.dmtf;

import com.github.mustachejava.DefaultMustacheFactory;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Redfish Code Generator for the Rust language
 * Based on swagger-parser v3
 */
public class RedfishCodegen {
    private String apiDirectory;
    private String crateDirectory;

    private IModelFileMapper[] mappers;

    private OpenAPI document;

    private FileFactory fileFactory;

    RedfishCodegen(String apiDirectory, String crateDirectory) {
        this.apiDirectory = apiDirectory;
        this.crateDirectory = crateDirectory;
        this.fileFactory = new FileFactory(new DefaultMustacheFactory(), this.crateDirectory,
                "src/" + RustConfig.MODELS_BASE_MODULE);

        this.mappers = new IModelFileMapper[4];
        this.mappers[0] = new VersionedModelMapper(this.fileFactory);
        this.mappers[1] = new SimpleModelMapper(Pattern.compile("Redfish(?<model>[a-zA-Z0-9]*)"), new SnakeCaseName("redfish"), this.fileFactory);
        this.mappers[2] = new SimpleModelMapper(Pattern.compile("odata-v4_(?<model>[a-zA-Z0-9]*)"), new SnakeCaseName("odata_v4"), this.fileFactory);
        this.mappers[3] = new UnversionedModelMapper(this.fileFactory);

        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        this.document = new OpenAPIV3Parser().read(this.apiDirectory + "/openapi.yaml", null, parseOptions);
        if (null == this.document) {
            throw new RuntimeException("Couldn't parse " + this.apiDirectory + "/openapi.yaml");
        }
    }

    public void generateModels() throws IOException {
        HashMap<String, ModuleFile> modules = new HashMap<>();
        for (Map.Entry<String, Schema> schema : this.document.getComponents().getSchemas().entrySet()) {
            schema.getValue().setName(schema.getKey());
            boolean handled = false;
            for (IModelFileMapper mapper : this.mappers) {
                ModelFile modelFile = mapper.matches(schema.getValue());
                if (null != modelFile) {
                    handled = true;
                    modelFile.registerModel(modules, this.fileFactory);
                    modelFile.generate();
                }
            }

            if (!handled) {
                System.out.println("[WARN] no handler matched model " + schema.getValue().getName());
            }
        }

        for (ModuleFile module : modules.values()) {
            module.generate();
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
