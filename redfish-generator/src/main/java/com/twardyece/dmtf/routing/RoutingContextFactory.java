package com.twardyece.dmtf.routing;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.api.TraitContext;
import com.twardyece.dmtf.registry.Version;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.PathItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutingContextFactory {
    private static final SnakeCaseName BODY = new SnakeCaseName("body");
    private static final Pattern PRIVILEGE_REGISTRY_PATTERN = Pattern.compile("Redfish_(?<version>[0-9.]+)_PrivilegeRegistry.json");
    private static final CratePath PRIVILEGE_PATH = CratePath.parse("redfish_core::auth");
    private final Map<RustType, PrivilegeMapping> privilegeMapping;
    public RoutingContextFactory(Path registryDirectory) throws IOException {
        Version maxVersion = null;
        for (String file : Objects.requireNonNull(registryDirectory.toFile().list())) {
            Matcher matcher = PRIVILEGE_REGISTRY_PATTERN.matcher(file);
            if (matcher.find()) {
                Version version = Version.parse(matcher.group("version"));
                if (maxVersion == null || 0 < maxVersion.compareTo(version)) {
                    maxVersion = version;
                }
            }
        }

        Path privilegeRegistry = registryDirectory.resolve("Redfish_" + maxVersion + "_PrivilegeRegistry.json");
        JSONObject object = new JSONObject(Files.readString(privilegeRegistry));
        JSONArray mappings = object.getJSONArray("Mappings");

        privilegeMapping = new HashMap<>();
        for (int i = 0; i < mappings.length(); ++i) {
            JSONObject mapping = mappings.getJSONObject(i);
            PascalCaseName entity = new PascalCaseName(mapping.getString("Entity"));
            for (String value : mapping.getJSONObject("OperationMap").keySet()) {
                PathItem.HttpMethod method = parseMethod(value);
                
            }
        }
    }

    public RoutingContext makeRoutingContext(TraitContext traitContext) {
        // PathMap is responsible for assigning submodules to each trait, so we rely on that mapping to be complete here.
        ModuleContext moduleContext = new ModuleContext(traitContext.moduleContext);
        CratePath routePath = translateTraitPath(traitContext.moduleContext.path);
        moduleContext.path = routePath;
        RustType rustType = new RustType(routePath, new PascalCaseName(traitContext.rustType.getName()));
        List<RoutingContext.Operation> operations = traitContext.operations
                .stream()
                .map(this::transformOperation)
                .toList();

        return new RoutingContext(moduleContext, rustType, traitContext.rustType, operations);
    }

    private static CratePath translateTraitPath(CratePath traitPath) {
        List<SnakeCaseName> components = traitPath.getComponents();
        for (int i = 0; i < components.size(); ++i) {
            if (components.get(i).equals(RustConfig.API_BASE_MODULE)) {
                components.set(i, RustConfig.ROUTING_BASE_MODULE);
            }
        }

        return CratePath.relative(components);
    }

    private RoutingContext.Operation transformOperation(TraitContext.Operation operation) {
        RoutingContext.Operation.Body body = null;
        List<RoutingContext.Operation.Parameter> parameters = new ArrayList<>();
        for (TraitContext.Parameter parameter : operation.parameters) {
            if (parameter.parameterName.equals(BODY)) {
                body = new RoutingContext.Operation.Body(parameter.type());
            } else {
                parameters.add(new RoutingContext.Operation.Parameter(parameter.name(), parameter.type()));
            }
        }
        return new RoutingContext.Operation(operation.name, operation.mutable, null, body, parameters);
    }

    private static PathItem.HttpMethod parseMethod(String method) {
        for (PathItem.HttpMethod value : PathItem.HttpMethod.values()) {
            if (0 == value.name().compareToIgnoreCase(method)) {
                return value;
            }
        }

        throw new RuntimeException("Unknown method " + method);
    }

    private record PrivilegeMapping(PathItem.HttpMethod method, RustType privilege) {}
}
