package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComponentContextFactory {
    private final PrivilegeRegistry privilegeRegistry;

    public ComponentContextFactory(PrivilegeRegistry privilegeRegistry) {
        this.privilegeRegistry = privilegeRegistry;
    }

    public ComponentContext makeComponentContext(RustType component, PathItem pathItem, List<RustType> subcomponents, List<RustType> owningComponents) {
        Map<PathItem.HttpMethod, RustType> privilegeMap = this.privilegeRegistry.getPrivilegesRequiredForComponent(component.getName().toString());
        List<ComponentContext.Operation> pathOperations = pathItem.readOperationsMap()
                .keySet()
                .stream()
                .map((method) -> new ComponentContext.Operation(nameForMethod(method), privilegeMap.get(method)))
                .toList();
        List<ComponentContext.Subcomponent> pathSubcomponents = subcomponents
                .stream()
                .map((sub) -> new ComponentContext.Subcomponent(sub.getName().toString().toLowerCase(), sub))
                .toList();

        ComponentContext componentContext = new ComponentContext(component, pathOperations, pathSubcomponents, owningComponents);
        return componentContext;
    }

    private static String nameForMethod(PathItem.HttpMethod method) {
        String name = "";
        switch (method) {
            case POST -> name = "post";
            case GET -> name = "get";
            case PUT -> name = "put";
            case PATCH -> name = "patch";
            case DELETE -> name = "delete";
            case HEAD -> name = "head";
            case OPTIONS -> name = "options";
            case TRACE -> name = "trace";
        }

        return name;
    }
}
