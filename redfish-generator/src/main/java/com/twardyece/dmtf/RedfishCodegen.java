package com.twardyece.dmtf;

import com.github.mustachejava.DefaultMustacheFactory;
import com.twardyece.dmtf.component.*;
import com.twardyece.dmtf.component.match.IComponentMatcher;
import com.twardyece.dmtf.component.match.StandardComponentMatcher;
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
import com.twardyece.dmtf.policies.IModelGenerationPolicy;
import com.twardyece.dmtf.policies.ODataPropertyPolicy;
import com.twardyece.dmtf.policies.ODataTypeIdentifier;
import com.twardyece.dmtf.policies.PropertyDefaultValueOverridePolicy;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
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
    private final String specDirectory;
    private final ModelResolver modelResolver;
    private final ComponentContextFactory componentContextFactory;
    private final IComponentMatcher[] componentMatchers;
    private final IModelGenerationPolicy[] modelGenerationPolicies;
    private final OpenAPI document;
    private final FileFactory fileFactory;
    private final RegistryFileDiscovery registryFileDiscovery;
    static final Logger LOGGER = LoggerFactory.getLogger(RedfishCodegen.class);

    RedfishCodegen(String specDirectory, String specVersion, String registryDirectory) throws IOException {
        this.specDirectory = specDirectory;
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

        DocumentParser parser = new DocumentParser(specDirectory + "/openapi/openapi.yaml");

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

        // Registry generation
        Path registryDirectoryPath = Path.of(registryDirectory);
        this.registryFileDiscovery = new RegistryFileDiscovery(registryDirectoryPath);

        PrivilegeRegistry privilegeRegistry = new PrivilegeRegistry(
                this.registryFileDiscovery
                        .getRegistry("PrivilegeMapping", Pattern.compile("Redfish_(?<version>[0-9.]+)_PrivilegeRegistry.json"))
                        .get()
                        .file,
                CratePath.parse("redfish_core::privilege"));
        this.componentContextFactory = new ComponentContextFactory(privilegeRegistry);
        this.componentMatchers = new IComponentMatcher[1];
        this.componentMatchers[0] = new StandardComponentMatcher();

        this.document = parser.parse();
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

        ModuleFile<LibContext> file = this.fileFactory.makeLibFile(this.specVersion);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.MODELS_BASE_MODULE);
        file.getContext().moduleContext.addNamedSubmodule(RustConfig.REGISTRY_BASE_MODULE);

        file.generate();
    }

    private void generateRouting() throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        ModuleFile<LibContext> libFile = this.fileFactory.makeLibFile(this.specVersion);

        // Metadata router, a submodule of the routing module that handles the OData metadata document.
        MetadataFileDiscovery fileDiscovery = new MetadataFileDiscovery(Path.of(this.specDirectory + "/csdl"));
        SnakeCaseName metadata = new SnakeCaseName("metadata");
        CratePath metadataPath = CratePath.parse("crate::" + metadata);
        MetadataRoutingContext metadataContext = new MetadataRoutingContext(new ModuleContext(metadataPath, null),
        fileDiscovery.getServiceRootVersion(), fileDiscovery.getReferences());
        ModuleFile<MetadataRoutingContext> metadataFile = this.fileFactory.makeMetadataRoutingFile(metadataContext);
        metadataFile.generate();
        libFile.getContext().moduleContext.addNamedSubmodule(metadata);

        // OData router, a submodule of the routing module that handles the OData service document.
        SnakeCaseName odata = new SnakeCaseName("odata");
        CratePath odataPath = CratePath.parse("crate::" + odata);
        ODataContext odataContext = new ODataContext(new ModuleContext(odataPath, null));
        ModuleFile<ODataContext> odataFile = this.fileFactory.makeODataRoutingFile(odataContext);
        odataFile.generate();
        libFile.getContext().moduleContext.addNamedSubmodule(odata);

        // The rest of the components
        PathMap map = new PathMap(this.document.getPaths(), this.componentContextFactory, this.modelResolver, this.componentMatchers);

        int pathDepth = libFile.getContext().moduleContext.path.getComponents().size();
        Iterator<ComponentContext> iterator = map.getComponentIterator();
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

    public void generate(String component) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        Map<String, ModuleFile<ModelContext>> models = this.buildModels();
        switch (component) {
            case "models" -> {
                this.generateModels(models);

                RustType messageType = this.getMessageType(models);
                RustType health = this.modelResolver.resolvePath("#/components/schemas/Resource_Health");
                RegistryFactory factory = new RegistryFactory(messageType, health);
                this.generateRegistries(factory);

            }
            case "routing" -> {
                this.generateRouting();
            }
            default -> throw new RuntimeException("Unknown component " + component);
        }
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
