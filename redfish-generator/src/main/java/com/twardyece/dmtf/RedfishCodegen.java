package com.twardyece.dmtf;

import com.github.mustachejava.DefaultMustacheFactory;
import com.twardyece.dmtf.component.*;
import com.twardyece.dmtf.component.match.ActionComponentMatcher;
import com.twardyece.dmtf.component.match.IComponentMatcher;
import com.twardyece.dmtf.component.match.StandardComponentMatcher;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.factory.*;
import com.twardyece.dmtf.model.mapper.*;
import com.twardyece.dmtf.policies.*;
import com.twardyece.dmtf.registry.RegistryContext;
import com.twardyece.dmtf.registry.RegistryFactory;
import com.twardyece.dmtf.registry.RegistryFileDiscovery;
import com.twardyece.dmtf.rust.RustConfig;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.specification.*;
import com.twardyece.dmtf.specification.file.DirectoryFileList;
import com.twardyece.dmtf.text.CamelCaseName;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Redfish Code Generator for the Rust language
 * Based on swagger-parser v3
 */
public class RedfishCodegen {
    private final String specVersion;
    private final String specDirectory;
    private final ModelResolver modelResolver;
    private final ComponentMatchService componentMatchService;
    private final IModelGenerationPolicy[] modelGenerationPolicies;
    private final OpenAPI document;
    private final FileFactory fileFactory;
    private final RegistryFileDiscovery registryFileDiscovery;
    static final Logger LOGGER = LoggerFactory.getLogger(RedfishCodegen.class);

    RedfishCodegen(String specDirectory, String specVersion, String registryDirectory) throws IOException {
        this.specDirectory = specDirectory;
        this.specVersion = specVersion;

        SimpleModelIdentifierFactory redfishModelIdentifierFactory = new SimpleModelIdentifierFactory(
                Pattern.compile("Redfish(?<model>[a-zA-Z0-9]*)"), "model",
                name -> "Redfish" + name);
        SimpleModelIdentifierFactory odataModelIdentifierFactory = new SimpleModelIdentifierFactory(
                Pattern.compile("odata-v4_(?<model>[a-zA-Z0-9]*)"), "model",
                name -> "odata-v4_" + new CamelCaseName(name));

        // Model generation setup
        IModelTypeMapper[] modelMappers = new IModelTypeMapper[4];
        modelMappers[0] = new VersionedModelTypeMapper();
        modelMappers[1] = new SimpleModelTypeMapper(redfishModelIdentifierFactory, new SnakeCaseName("redfish"));
        modelMappers[2] = new SimpleModelTypeMapper(odataModelIdentifierFactory, new SnakeCaseName("odata_v4"));
        modelMappers[3] = new UnversionedModelTypeMapper();

        NamespaceMapper[] namespaceMappers = new NamespaceMapper[1];
        Pattern odataModelPattern = Pattern.compile("odata_v?4_0_[0-9]_");
        namespaceMappers[0] = new NamespaceMapper(odataModelPattern, "odata-v4_");
        this.modelResolver = new ModelResolver(modelMappers, namespaceMappers);
        IModelContextFactory[] factories = new IModelContextFactory[6];
        factories[0] = new EnumContextFactory();
        factories[1] = new FreeFormObjectContextFactory();
        factories[2] = new StructContextFactory(this.modelResolver);
        factories[3] = new TupleContextFactory(this.modelResolver);
        SimpleModelIdentifierFactory[] identifierParsers = new SimpleModelIdentifierFactory[2];
        identifierParsers[0] = odataModelIdentifierFactory;
        identifierParsers[1] = new SimpleModelIdentifierFactory(
                Pattern.compile("^Resource_(?<model>[a-zA-Z0-9]*)$"), "model",
                name -> "Resource_" + name);
        factories[4] = new UnionContextFactory(this.modelResolver, new UnionVariantParser(identifierParsers));
        factories[5] = new UnitContextFactory();
        this.fileFactory = new FileFactory(new DefaultMustacheFactory(), factories);

        // These intrusive/low-level policies need to be applied to the set of models as a whole, but should not be
        // coupled to context factories.
        this.modelGenerationPolicies = new IModelGenerationPolicy[4];
        this.modelGenerationPolicies[0] = new ModelDeletionPolicy(odataModelPattern);
        this.modelGenerationPolicies[1] = new OemModelDeletionPolicy();
        this.modelGenerationPolicies[2] = new ODataPropertyPolicy(new ODataTypeIdentifier());
        JsonSchemaMapper[] jsonSchemaMappers = new JsonSchemaMapper[2];

        Pattern versionParsePattern = Pattern.compile("([0-9]+)_([0-9]+)_([0-9]+)");
        VersionedFileDiscovery versionedFileDiscovery = new VersionedFileDiscovery(new DirectoryFileList(Paths.get(specDirectory + "/json-schema")));
        Optional<VersionedFileDiscovery.VersionedFile> redfishErrorJsonSchema = versionedFileDiscovery.getFile(
                "redfish-error", Pattern.compile("redfish-error.v(?<version>" + versionParsePattern + ").json"), "version", versionParsePattern);
        if (redfishErrorJsonSchema.isEmpty()) {
            throw new RuntimeException("Could not locate the redfish-error json-schema file!");
        }
        jsonSchemaMappers[0] = new JsonSchemaMapper(
                redfishModelIdentifierFactory,
                redfishErrorJsonSchema.get().file.getFileName().toString());

        Optional<VersionedFileDiscovery.VersionedFile> odataJsonSchema = versionedFileDiscovery.getFile(
                "odata", Pattern.compile("odata.v(?<version>" + versionParsePattern + ").json"), "version", versionParsePattern);
        if (odataJsonSchema.isEmpty()) {
            throw new RuntimeException("Could not locate the odata json-schema file!");
        }
        jsonSchemaMappers[1] = new JsonSchemaMapper(
                odataModelIdentifierFactory,
                odataJsonSchema.get().file.getFileName().toString());
        this.modelGenerationPolicies[3] = new ModelMetadataPolicy(new JsonSchemaIdentifier(jsonSchemaMappers));

        // Registry generation
        Path registryDirectoryPath = Path.of(registryDirectory);
        this.registryFileDiscovery = new RegistryFileDiscovery(registryDirectoryPath);

        PrivilegeRegistry privilegeRegistry = new PrivilegeRegistry(
                this.registryFileDiscovery
                        .getRegistry("PrivilegeMapping", Pattern.compile("Redfish_(?<version>[0-9.]+)_PrivilegeRegistry.json"))
                        .get()
                        .file,
                CratePath.parse("redfish_core::privilege"));
        IComponentMatcher[] componentMatchers = new IComponentMatcher[2];
        List<Pair<PathItem.HttpMethod, String>> unprotectedOperations = new ArrayList<>();
        unprotectedOperations.add(new ImmutablePair<>(PathItem.HttpMethod.GET, "/redfish/v1"));
        unprotectedOperations.add(new ImmutablePair<>(PathItem.HttpMethod.POST, "/redfish/v1/SessionService/Sessions"));
        componentMatchers[0] = new StandardComponentMatcher(
                privilegeRegistry,
                new ComponentTypeTranslationService(this.modelResolver),
                unprotectedOperations);
        componentMatchers[1] = new ActionComponentMatcher();
        this.componentMatchService = new ComponentMatchService(componentMatchers, new PathService());

        // The OpenapiSpecification will automatically generate names for inlined schemas. Having run the tool and seen (in the
        // console output) that these schemas are assigned autogenerated names, we choose to assign more meaningful names
        // here.
        Map<String, String> inlineSchemaNameMappings = new HashMap<>();
        inlineSchemaNameMappings.put("RedfishError_error", "RedfishRedfishError");
        inlineSchemaNameMappings.put("_redfish_v1_odata_get_200_response", "odata-v4_ServiceDocument");
        inlineSchemaNameMappings.put("_redfish_v1_odata_get_200_response_value_inner", "odata-v4_Service");

        Pattern[] ignoredSchemaFiles = new Pattern[2];
        ignoredSchemaFiles[0] = Pattern.compile("^odata.*$");
        ignoredSchemaFiles[1] = Pattern.compile("^redfish-payload-annotations-.*$");
        OpenapiSpecification specification = new OpenapiSpecification(Path.of(specDirectory), inlineSchemaNameMappings,
                ignoredSchemaFiles);
        this.document = specification.getRedfishDataModel();
    }

    private Map<String, ModuleFile<ModelContext>> buildModels() {
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

        return models;
    }

    private void generateModels(Map<String, ModuleFile<ModelContext>> models) throws IOException {
        // Generate all the models
        Map<String, ModuleContext> intermediateModules = new HashMap<>();
        for (ModuleFile<ModelContext> modelFile : models.values()) {
            modelFile.getContext().moduleContext.registerModule(intermediateModules);
            modelFile.generate();
        }

        // Generate intermediate modules
        for (ModuleContext module : intermediateModules.values()) {
            ModuleFile<ModuleContext> file = this.fileFactory.makeModuleFile(module);
            file.generate();
        }
    }

    public void generateModelsLib() throws IOException {
        ModuleContext module = new ModuleContext(CratePath.crateRoot());
        LibContext context = new LibContext(module, specVersion, getResourceFileAsString("codegen.rs"));
        ModuleFile<LibContext> file = this.fileFactory.makeLibFile(context);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.MODELS_BASE_MODULE);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.REGISTRY_BASE_MODULE);
        file.generate();
    }

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    private void generateRouting(Map<PascalCaseName, RegistryContext> registries) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        ModuleContext moduleContext = new ModuleContext(CratePath.crateRoot());
        LibContext libContext = new LibContext(moduleContext, this.specVersion);
        ModuleFile<LibContext> libFile = this.fileFactory.makeLibFile(libContext);
        Map<String, PathItem> paths = this.document.getPaths();

        // Metadata router, a submodule of the routing module that handles the OData metadata document.
        MetadataFileDiscovery fileDiscovery = new MetadataFileDiscovery(Path.of(this.specDirectory + "/csdl"));
        SnakeCaseName metadata = new SnakeCaseName("metadata");
        CratePath metadataPath = CratePath.parse("crate::" + metadata);
        MetadataRoutingContext metadataContext = new MetadataRoutingContext(new ModuleContext(metadataPath),
        fileDiscovery.getServiceRootVersion(), fileDiscovery.getReferences());
        ModuleFile<MetadataRoutingContext> metadataFile = this.fileFactory.makeMetadataRoutingFile(metadataContext);
        metadataFile.generate();
        libFile.getContext().moduleContext.addNamedSubmodule(metadata);
        paths.remove("/redfish/v1/$metadata");

        // OData router, a submodule of the routing module that handles the OData service document.
        SnakeCaseName odata = new SnakeCaseName("odata");
        CratePath odataPath = CratePath.parse("crate::" + odata);
        ODataContext odataContext = new ODataContext(new ModuleContext(odataPath));
        ModuleFile<ODataContext> odataFile = this.fileFactory.makeODataRoutingFile(odataContext);
        odataFile.generate();
        libFile.getContext().moduleContext.addNamedSubmodule(odata);
        paths.remove("/redfish/v1/odata");

        // The rest of the components
        RegistryContext baseRegistry = registries.get(new PascalCaseName("Base"));
        baseRegistry.rustType.getPath().getComponents().set(0, new SnakeCaseName("redfish_codegen"));
        int pathDepth = libFile.getContext().moduleContext.path.getComponents().size();
        ComponentRepository componentRepository = new ComponentRepository(
                new ComponentTypeTranslationService(this.modelResolver),
                new PathService(),
                baseRegistry.rustType);
        Iterator<ComponentContext> iterator = this.componentMatchService.getComponents(paths, componentRepository);
        while (iterator.hasNext()) {
            ComponentContext component = iterator.next();
            if (component.moduleContext.path.getComponents().size() == pathDepth + 1) {
                libFile.getContext().moduleContext.addNamedSubmodule(component.moduleContext.path.getLastComponent());
            }
            ModuleFile<ComponentContext> traitFile = this.fileFactory.makeTraitFile(component);
            traitFile.generate();
        }

        libFile.generate();
    }

    private Map<PascalCaseName, RegistryContext> buildRegistries(RegistryFactory factory) throws IOException {
        CratePath registryModulePath = CratePath.parse("crate::" + RustConfig.REGISTRY_BASE_MODULE);
        Map<PascalCaseName, RegistryContext> registryContextMap = new HashMap<>();
        for (RegistryFileDiscovery.Registry registry : this.registryFileDiscovery.getRegistries()) {
            CratePath path = registryModulePath
                    .append(CaseConversion.toSnakeCase(registry.name))
                    .append(new SnakeCaseName("v" + registry.version));
            PascalCaseName name = new PascalCaseName(registry.name);
            RegistryContext registryContext = factory.makeRegistry(new RustType(path, name), registry.file);
            registryContextMap.put(name, registryContext);
        }

        return registryContextMap;
    }

    private void generateRegistries(Map<PascalCaseName, RegistryContext> registries) throws IOException {
        CratePath registryModulePath = CratePath.parse("crate::" + RustConfig.REGISTRY_BASE_MODULE);
        ModuleContext registriesModule = new ModuleContext(registryModulePath);

        for (RegistryContext registry : registries.values()) {
            registriesModule.addNamedSubmodule(CaseConversion.toSnakeCase(registry.name()));
            CratePath path = registryModulePath;
            List<SnakeCaseName> components = registry.rustType.getPath().getComponents();
            List<ModuleContext> moduleContexts = new ArrayList<>();
            ModuleContext previous = null;
            for (SnakeCaseName component : components.subList(registryModulePath.getComponents().size(), components.size() - 1)) {
                path = path.append(component);
                ModuleContext current = new ModuleContext(path);
                moduleContexts.add(current);
                Objects.requireNonNullElse(previous, registriesModule).addNamedSubmodule(component);
                previous = current;
            }

            previous.addNamedSubmodule(registry.rustType.getPath().getLastComponent());
            ModuleFile<RegistryContext> registryFile = this.fileFactory.makeRegistryFile(registry);

            for (ModuleContext context : moduleContexts) {
                ModuleFile<ModuleContext> file = this.fileFactory.makeModuleFile(context);
                file.generate();
            }

            registryFile.generate();
        }

        ModuleFile<ModuleContext> registriesFile = this.fileFactory.makeModuleFile(registriesModule);
        registriesFile.generate();
    }

    public void generate(String component) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        Map<String, ModuleFile<ModelContext>> models = this.buildModels();

        RustType messageType = this.getMessageType(models);
        RustType health = this.modelResolver.resolvePath("#/components/schemas/Resource_Health");
        RegistryFactory factory = new RegistryFactory(messageType, health);
        Map<PascalCaseName, RegistryContext> registries = this.buildRegistries(factory);
        switch (component) {
            case "models" -> {
                this.generateModels(models);
                this.generateRegistries(registries);
                this.generateModelsLib();
            }
            case "routing" -> this.generateRouting(registries);
            default -> throw new RuntimeException("Unknown component " + component);
        }
    }

    private RustType getMessageType(Map<String, ModuleFile<ModelContext>> models) {
        // TODO: Utilize VersionedFileDiscovery for this?
        Version latestVersion = models.keySet().stream()
                .filter((k) -> k.startsWith("Message_"))
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
        Option specDirectoryOption = new Option("specDirectory", true,
                "Directory containing openapi and CSDL resource files");
        specDirectoryOption.setRequired(true);
        Option crateDirectoryOption = new Option("specVersion", true,
                "The version of the redfish data model specification that will be provided by the generated crate");
        crateDirectoryOption.setRequired(true);
        Option registryDirectoryOption = new Option("registryDirectory", true,
                "Directory containing registry definition files");
        registryDirectoryOption.setRequired(true);
        Option componentOption = new Option("component", true, "Data model component to generate");
        componentOption.setRequired(true);

        Options options = new Options();
        options.addOption(specDirectoryOption);
        options.addOption(crateDirectoryOption);
        options.addOption(registryDirectoryOption);
        options.addOption(componentOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine command = parser.parse(options, args);

            String specDirectory = command.getOptionValue("specDirectory");
            String specVersion = command.getOptionValue("specVersion");
            String registryDirectory = command.getOptionValue("registryDirectory");
            String component = command.getOptionValue("component");

            RedfishCodegen codegen = new RedfishCodegen(specDirectory, specVersion, registryDirectory);
            codegen.generate(component);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("RedfishCodegen", options);
            System.exit(1);
        } catch (IOException | URISyntaxException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
