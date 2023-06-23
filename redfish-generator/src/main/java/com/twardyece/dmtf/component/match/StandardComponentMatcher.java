package com.twardyece.dmtf.component.match;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.component.ComponentContext;
import com.twardyece.dmtf.component.ComponentRepository;
import com.twardyece.dmtf.component.ComponentTypeTranslationService;
import com.twardyece.dmtf.component.PrivilegeRegistry;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StandardComponentMatcher implements IComponentMatcher {

    private final PrivilegeRegistry privilegeRegistry;
    private final ComponentTypeTranslationService componentTypeTranslationService;
    private final List<Pair<PathItem.HttpMethod, String>> unprotectedOperations;
    private static final ArrayList<PathItem.HttpMethod> METHODS = new ArrayList<>();

    static {
        METHODS.add(PathItem.HttpMethod.GET);
        METHODS.add(PathItem.HttpMethod.HEAD);
        METHODS.add(PathItem.HttpMethod.POST);
        METHODS.add(PathItem.HttpMethod.PUT);
        METHODS.add(PathItem.HttpMethod.PATCH);
        METHODS.add(PathItem.HttpMethod.DELETE);
    }


    public StandardComponentMatcher(PrivilegeRegistry privilegeRegistry,
                                    ComponentTypeTranslationService componentTypeTranslationService,
                                    List<Pair<PathItem.HttpMethod, String>> unprotectedOperations) {
        this.privilegeRegistry = privilegeRegistry;
        this.componentTypeTranslationService = componentTypeTranslationService;
        this.unprotectedOperations = unprotectedOperations;
    }

    @Override
    public Optional<ComponentContext> matchUri(ComponentRepository repository, String uri, PathItem pathItem) {
        Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();
        if (!operations.containsKey(PathItem.HttpMethod.GET)) {
            return Optional.empty();
        }

        // Perhaps a weak policy, but for now we assume that the component is the body type of the 200 response to
        // the GET request.
        MediaType mediaType = operations
                .get(PathItem.HttpMethod.GET)
                .getResponses()
                .get("200")
                .getContent()
                .get("application/json");
        if (null == mediaType) {
            return Optional.empty();
        }

        Schema schema = mediaType.getSchema();
        if (null == schema) {
            return Optional.empty();
        }

        ComponentContext context = repository.getOrCreateComponent(schema.get$ref(), uri);
        this.updateContext(context, uri, pathItem);
        return Optional.of(context);
    }

    private void updateContext(ComponentContext context, String uri, PathItem pathItem) {
        pathItem.readOperationsMap()
                .keySet()
                .stream()
                .filter(METHODS::contains)
                .filter((method) -> !context.operationMap.containsKey(method))
                .forEach((method) -> {
                    boolean requiresAuth = this.unprotectedOperations
                            .stream()
                            .filter((op) -> op.getLeft().equals(method) && op.getRight().equals(uri))
                            .findAny()
                            .isEmpty();
                    context.operationMap.put(
                            method,
                            new ComponentContext.Operation(
                                    ComponentContext.operationNameForMethod(method),
                                    requiresAuth
                            )
                    );
                });

        PascalCaseName componentName = new PascalCaseName(context.rustType.getName());
        context.defaultPrivileges = privilegeRegistry.getPrivilegesForComponent(
                componentName);
        List<Pair<String, PrivilegeRegistry.OperationPrivilegeMapping>> overrides = privilegeRegistry
                .getSubordinatePrivilegeOverridesForComponent(componentName);
        for (Pair<String, PrivilegeRegistry.OperationPrivilegeMapping> override : overrides) {
            Optional<ComponentContext.SubordinatePrivilegeOverride> existing = context.subordinatePrivilegeOverrides
                    .stream()
                    .filter((o) -> o.owningComponent().getName().toString().equals(override.getLeft()))
                    .findFirst();
            if (existing.isEmpty()) {
                RustType rustType = this.componentTypeTranslationService
                        .getRustTypeForComponentName(override.getLeft());
                context.subordinatePrivilegeOverrides.add(
                        new ComponentContext.SubordinatePrivilegeOverride(
                                rustType,
                                new PascalCaseName(rustType.getName()),
                                override.getRight())
                );
            }
        }
    }
}
