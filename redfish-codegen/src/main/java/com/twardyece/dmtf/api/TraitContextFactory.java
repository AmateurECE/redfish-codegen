package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.*;
import java.util.stream.Collectors;

public class TraitContextFactory {
    private final ModelResolver modelResolver;
    private final EndpointResolver endpointResolver;
    private final Map<PascalCaseName, PascalCaseName> traitNameOverrides;
    public TraitContextFactory(ModelResolver modelResolver, EndpointResolver endpointResolver,
                               Map<PascalCaseName, PascalCaseName> traitNameOverrides) {
        this.modelResolver = modelResolver;
        this.endpointResolver = endpointResolver;
        this.traitNameOverrides = traitNameOverrides;
    }

    public TraitContext makeTraitContext(List<String> path, PathItem pathItem, List<String> mountpoints) {
        EndpointResolver.ApiMatchResult result = endpointResolver.resolve(path);
        if (traitNameOverrides.containsKey(result.name)) {
            result.name = traitNameOverrides.get(result.name);
        }

        Map<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> pathOperations = pathItem.readOperationsMap();
        List<TraitContext.Operation> operations = pathOperations.entrySet()
                .stream()
                .map(this::makeOperation)
                .collect(Collectors.toList());

        List<CratePath> imports = this.getImports(operations);
        if (imports.isEmpty()) {
            imports = null;
        }

        return new TraitContext(result.path, result.name, imports, mountpoints, operations);
    }

    private TraitContext.Operation makeOperation(Map.Entry<PathItem.HttpMethod, Operation> entry) {
        List<Parameter> pathParameters = entry.getValue().getParameters();
        List<TraitContext.Parameter> parameters = null;
        if (null != pathParameters) {
            parameters = pathParameters
                    .stream()
                    .map(this::makeParameter)
                    .collect(Collectors.toList());
        }

        RequestBody requestBody = entry.getValue().getRequestBody();
        if (null != requestBody) {
            Content content = requestBody.getContent();
            if (null != content) {
                if (null == parameters) {
                    parameters = new ArrayList<>();
                }
                List<String> contentTypes = content.keySet().stream().toList();
                // TODO: Must add supporting type here if contentTypes.size() > 1
                parameters.add(new TraitContext.Parameter(new SnakeCaseName("body"),
                        this.modelResolver.resolveSchema(content.get(contentTypes.get(0)).getSchema())));
            }
        }

        ApiResponses responses = entry.getValue().getResponses();
        List<String> codes = responses.keySet().stream().toList();
        Content firstResponse = responses.get(codes.get(0)).getContent();
        List<String> contentTypes = firstResponse.keySet().stream().toList();
        Schema schema = firstResponse.get(contentTypes.get(0)).getSchema();
        TraitContext.ReturnType returnType = null;
        if (null != schema) {
            // TODO: Must add supporting type here if contentTypes.size() > 1
            returnType = new TraitContext.ReturnType(this.modelResolver.resolveSchema(schema));
        }


        return new TraitContext.Operation(entry.getKey(), parameters, returnType);
    }

    private TraitContext.Parameter makeParameter(Parameter parameter) {
        return new TraitContext.Parameter(CaseConversion.toSnakeCase(parameter.getName()),
                this.modelResolver.resolveSchema(parameter.getSchema()));
    }

    private List<CratePath> getImports(List<TraitContext.Operation> operations) {
        Set<CratePath> imports = new HashSet<>();
        for (TraitContext.Operation operation : operations) {
            if (null != operation.parameters) {
                for (TraitContext.Parameter parameter : operation.parameters) {
                    CratePath importPath = this.configureImportPath(parameter.rustType);
                    if (null != importPath) {
                        imports.add(importPath);
                    }
                }
            }

            if (null != operation.returnType) {
                CratePath importPath = this.configureImportPath(operation.returnType.rustType);
                if (null != importPath) {
                    imports.add(importPath);
                }
            }
        }

        return imports.stream().toList();
    }

    private CratePath configureImportPath(RustType rustType) {
        CratePath path = rustType.getPath();
        if (null == path) {
            return null;
        }

        List<SnakeCaseName> components = path.getComponents();
        if (2 < components.size()) {
            List<SnakeCaseName> relativeImport = new ArrayList<>();
            relativeImport.add(components.get(0));
            relativeImport.add(components.get(1));
            components.remove(0);
            rustType.setImportPath(CratePath.relative(components));
            return CratePath.relative(relativeImport);
        } else {
            return null;
        }
    }
}
