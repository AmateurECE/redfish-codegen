package com.twardyece.dmtf;

import com.github.mustachejava.DefaultMustacheFactory;
import com.twardyece.dmtf.api.*;
import com.twardyece.dmtf.api.name.DetailNameMapper;
import com.twardyece.dmtf.api.name.INameMapper;
import com.twardyece.dmtf.api.name.NameMapper;
import com.twardyece.dmtf.identifiers.IdentifierParseError;
import com.twardyece.dmtf.identifiers.VersionedSchemaIdentifier;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.factory.*;
import com.twardyece.dmtf.model.mapper.IModelFileMapper;
import com.twardyece.dmtf.model.mapper.SimpleModelMapper;
import com.twardyece.dmtf.model.mapper.UnversionedModelMapper;
import com.twardyece.dmtf.model.mapper.VersionedModelMapper;
import com.twardyece.dmtf.openapi.DocumentParser;
import com.twardyece.dmtf.policies.*;
import com.twardyece.dmtf.registry.RegistryContext;
import com.twardyece.dmtf.registry.RegistryFactory;
import com.twardyece.dmtf.registry.RegistryFileDiscovery;
import com.twardyece.dmtf.registry.Version;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redfish Code Generator for the Rust language
 * Based on swagger-parser v3
 */
public class RedfishCodegen {
    private final String specVersion;
    private final ModelResolver modelResolver;
    private final TraitContextFactory traitContextFactory;
    private final IModelGenerationPolicy[] modelGenerationPolicies;
    private final IApiGenerationPolicy[] apiGenerationPolicies;
    private final OpenAPI document;
    private final FileFactory fileFactory;
    private final RegistryFileDiscovery registryFileDiscovery;
    static final Logger LOGGER = LoggerFactory.getLogger(RedfishCodegen.class);

    RedfishCodegen(String apiDirectory, String specVersion, String registryDirectory) {
        this.specVersion = specVersion;

        // Model generation setup
        IModelFileMapper[] modelMappers = new IModelFileMapper[4];
        modelMappers[0] = new VersionedModelMapper();
        modelMappers[1] = new SimpleModelMapper(Pattern.compile("Redfish(?<model>[a-zA-Z0-9]*)"), new SnakeCaseName("redfish"));
        modelMappers[2] = new SimpleModelMapper(Pattern.compile("odata-v4_(?<model>[a-zA-Z0-9]*)"), new SnakeCaseName("odata_v4"));
        modelMappers[3] = new UnversionedModelMapper();

        this.modelResolver = new ModelResolver(modelMappers);
        IModelContextFactory[] factories = new IModelContextFactory[5];
        factories[0] = new EnumContextFactory();
        factories[1] = new FreeFormObjectContextFactory();
        factories[2] = new StructContextFactory(this.modelResolver);
        factories[3] = new TupleContextFactory(this.modelResolver);
        factories[4] = new UnionContextFactory(this.modelResolver, new UnionVariantParser());
        this.fileFactory = new FileFactory(new DefaultMustacheFactory(), factories);

        DocumentParser parser = new DocumentParser(apiDirectory + "/openapi.yaml");

        // The DocumentParser will automatically generate names for inlined schemas. Having run the tool and seen (in the
        // console output) that these schemas are assigned autogenerated names, we choose to assign more meaningful names
        // here.
        parser.addInlineSchemaNameMapping("RedfishError_error", "RedfishRedfishError");
        parser.addInlineSchemaNameMapping("_redfish_v1_odata_get_200_response", "odata-v4_ServiceDocument");
        parser.addInlineSchemaNameMapping("_redfish_v1_odata_get_200_response_value_inner", "odata-v4_Service");

        // These intrusive/low-level policies need to be applied to the set of models as a whole, but should not be
        // coupled to context factories.
        this.modelGenerationPolicies = new IModelGenerationPolicy[2];
        this.modelGenerationPolicies[0] = new ODataPropertyPolicy(new ODataTypeIdentifier());
        Map<Pair<String, String>, String> overrides = new HashMap<>();
        overrides.put(new ImmutablePair<>("odata-v4_Service", "kind"), "\\\"Singleton\\\".to_string()");
        this.modelGenerationPolicies[1] = new PropertyDefaultValueOverridePolicy(overrides);

        // API generation setup
        List<INameMapper> nameMappers = new ArrayList<>();
        nameMappers.add(new NameMapper(Pattern.compile("^(?<name>[A-Za-z0-9]+)$"), "name"));
        nameMappers.add(new DetailNameMapper());
        nameMappers.add(new NameMapper(Pattern.compile("(?<=\\.)(?<name>[A-Za-z0-9]+)$"), "name"));
        nameMappers.add(new NameMapper(Pattern.compile("^\\$(?<name>metadata)$"), "name"));
        EndpointResolver endpointResolver = new EndpointResolver(nameMappers);

        Map<PascalCaseName, PascalCaseName> traitNameOverrides = new HashMap<>();
        traitNameOverrides.put(new PascalCaseName("V1"), new PascalCaseName("ServiceRoot"));

        this.traitContextFactory = new TraitContextFactory(this.modelResolver, endpointResolver, traitNameOverrides);

        this.apiGenerationPolicies = new IApiGenerationPolicy[1];
        this.apiGenerationPolicies[0] = new PatchRequestBodyTypePolicy();

        // Registry generation
        this.registryFileDiscovery = new RegistryFileDiscovery(Path.of(registryDirectory));

        this.document = parser.parse();
    }

    private Map<String, ModuleFile<ModelContext>> generateModels() throws IOException {
        // Translate each schema into a ModuleFile with associated model context
        Map<String, ModuleFile<ModelContext>> models = new HashMap<>();
        for (Map.Entry<String, Schema> schema : this.document.getComponents().getSchemas().entrySet()) {
            RustType result = this.modelResolver.resolvePath(schema.getKey());
            if (null == result) {
                LOGGER.warn("no match for model " + schema.getValue().getName());
                continue;
            }

            ModuleFile<ModelContext> modelFile = this.fileFactory.makeModelFile(result, schema.getValue());
            if (null != modelFile) {
                models.put(schema.getKey(), modelFile);
            }
        }

        // Apply model generation policies
        for (IModelGenerationPolicy policy : this.modelGenerationPolicies) {
            policy.apply(models);
        }

        // Generate all the models
        Map<String, ModuleContext> intermediateModules = new HashMap<>();
        for (ModuleFile<ModelContext> modelFile : models.values()) {
            modelFile.getContext().moduleContext.registerModel(intermediateModules);
            modelFile.generate();
        }

        // Generate intermediate modules
        for (ModuleContext module : intermediateModules.values()) {
            ModuleFile<ModuleContext> file = this.fileFactory.makeModuleFile(module);
            file.generate();
        }

        return models;
    }

    private void generateApis() throws IOException {
        PathMap map = new PathMap(this.document.getPaths(), this.traitContextFactory);
        for (IApiGenerationPolicy policy : this.apiGenerationPolicies) {
            policy.apply(map.borrowGraph(), map.getRoot());
        }

        List<SnakeCaseName> apiModulePathComponents = new ArrayList<>();
        apiModulePathComponents.add(RustConfig.API_BASE_MODULE);
        CratePath apiModulePath = CratePath.crateLocal(apiModulePathComponents);
        ModuleContext apiModule = new ModuleContext(apiModulePath, null);
        int pathDepth = apiModulePath.getComponents().size();

        for (TraitContext trait : map.getTraits()) {
            if (trait.moduleContext.path.getComponents().size() == pathDepth + 1) {
                apiModule.addNamedSubmodule(trait.moduleContext.path.getLastComponent());
            }
            ModuleFile<TraitContext> file = this.fileFactory.makeTraitFile(trait);
            file.generate();
        }

        ModuleFile<ModuleContext> apiFile = this.fileFactory.makeModuleFile(apiModule);
        apiFile.generate();
    }

    private void generateLib() throws IOException {
        ModuleFile<LibContext> file = this.fileFactory.makeLibFile(this.specVersion);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.API_BASE_MODULE);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.MODELS_BASE_MODULE);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.REGISTRY_BASE_MODULE);

        file.generate();
    }

    private void generateRegistries(RegistryFactory factory) throws IOException {
        List<SnakeCaseName> components = new ArrayList<>();
        components.add(RustConfig.REGISTRY_BASE_MODULE);
        CratePath registryModulePath = CratePath.crateLocal(components);
        ModuleContext registriesModule = new ModuleContext(registryModulePath, null);

        List<RegistryFileDiscovery.Registry> registries = this.registryFileDiscovery.getRegistries();
        for (RegistryFileDiscovery.Registry registry : registries) {
            registriesModule.addNamedSubmodule(CaseConversion.toSnakeCase(registry.name));

            CratePath parentPath = registryModulePath.append(CaseConversion.toSnakeCase(registry.name));
            ModuleContext registryContext = new ModuleContext(parentPath, null);

            SnakeCaseName version = new SnakeCaseName("v" + registry.version);
            CratePath registryPath = parentPath.append(version);
            RegistryContext context = factory.makeRegistry(
                    new RustType(registryPath, new PascalCaseName(registry.name)), registry.file);

            registryContext.addNamedSubmodule(version);

            ModuleFile<ModuleContext> registryFile = this.fileFactory.makeModuleFile(registryContext);
            registryFile.generate();

            ModuleFile<RegistryContext> file = this.fileFactory.makeRegistryFile(context);
            file.generate();
        }

        ModuleFile<ModuleContext> registriesFile = this.fileFactory.makeModuleFile(registriesModule);
        registriesFile.generate();
    }

    public void generate() throws IOException {
        Map<String, ModuleFile<ModelContext>> models = this.generateModels();

        this.generateApis();
        this.generateLib();

        RustType messageType = this.getMessageType(models);
        RustType health = this.modelResolver.resolvePath("Resource_Health");
        RegistryFactory factory = new RegistryFactory(messageType, health);
        this.generateRegistries(factory);
    }

    private RustType getMessageType(Map<String, ModuleFile<ModelContext>> models) {
        Pattern messagePattern = Pattern.compile("^Message_.*");
        Version latestVersion = models.keySet().stream()
                .filter((k) -> {
                    Matcher matcher = messagePattern.matcher(k);
                    return matcher.find();
                })
                .map((m) -> {
                    try {
                        VersionedSchemaIdentifier identifier = new VersionedSchemaIdentifier(m);
                        return Version.parse(identifier.getVersion().toString(),
                                Pattern.compile("v([0-9]+)_([0-9]+)_([0-9]+)"));
                    } catch (IdentifierParseError e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .max(Version::compareTo)
                .get();

        String messageModel = "Message_v" + latestVersion.major + "_" + latestVersion.minor + "_" + latestVersion.patch
                + "_Message";
        return models.get(messageModel).getContext().rustType;
    }

    public static void main(String[] args) {
        Option apiDirectoryOption = new Option("apiDirectory", true,
                "Directory containing openapi resource files");
        apiDirectoryOption.setRequired(true);
        Option crateDirectoryOption = new Option("specVersion", true,
                "The version of the redfish data model specification that will be provided by the generated crate");
        crateDirectoryOption.setRequired(true);
        Option registryDirectoryOption = new Option("registryDirectory", true,
                "Directory containing registry definition files");
        registryDirectoryOption.setRequired(true);

        Options options = new Options();
        options.addOption(apiDirectoryOption);
        options.addOption(crateDirectoryOption);
        options.addOption(registryDirectoryOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine command = parser.parse(options, args);

            String apiDirectory = command.getOptionValue("apiDirectory");
            String specVersion = command.getOptionValue("specVersion");
            String registryDirectory = command.getOptionValue("registryDirectory");

            RedfishCodegen codegen = new RedfishCodegen(apiDirectory, specVersion, registryDirectory);
            codegen.generate();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("RedfishCodegen", options);
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
