package com.twardyece.dmtf.routing;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TraitContextFactory {
    private final ModelResolver modelResolver;
    private final EndpointResolver endpointResolver;
    private final Map<PascalCaseName, PascalCaseName> traitNameOverrides;

    private static final RustType httpBody = new RustType(CratePath.parse("hyper::body"), new PascalCaseName("Body"));

    public TraitContextFactory(ModelResolver modelResolver, EndpointResolver endpointResolver,
                               Map<PascalCaseName, PascalCaseName> traitNameOverrides) {
        this.modelResolver = modelResolver;
        this.endpointResolver = endpointResolver;
        this.traitNameOverrides = traitNameOverrides;
    }

    private EndpointResolver.ApiMatchResult getMatch(List<String> path) {
        EndpointResolver.ApiMatchResult result = endpointResolver.resolve(path);
        if (traitNameOverrides.containsKey(result.name)) {
            result.name = traitNameOverrides.get(result.name);
        }

        return result;
    }

    public RustType getRustType(List<String> path) {
        EndpointResolver.ApiMatchResult result = getMatch(path);
        return new RustType(result.path, result.name);
    }

    public TraitContext makeTraitContext(List<String> path, PathItem pathItem, List<String> mountpoints) {
        EndpointResolver.ApiMatchResult result = getMatch(path);

        Map<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> pathOperations = pathItem.readOperationsMap();
        List<ModelContext> supportingTypes = new ArrayList<>();
        List<TraitContext.Operation> operations = new ArrayList<>();

        PathItemParseContext context = new PathItemParseContext();
        context.path = result.path;
        context.traitName = result.name;

        for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathOperations.entrySet()) {
            context.operation = entry.getValue();
            context.method = entry.getKey();
            context.methodName = nameForMethod(entry.getKey());

            PathItemParseResult parseResult = this.makeOperation(context);

            operations.add(parseResult.operation);
            if (null != parseResult.supportingTypes) {
                supportingTypes.addAll(parseResult.supportingTypes);
            }
        }

        TraitContext traitContext = new TraitContext(new RustType(result.path, result.name), supportingTypes, operations, mountpoints);
        // Supporting types will have their own imports. Make sure to include them in the top-level module context
        for (ModelContext model : traitContext.supportingTypes) {
            traitContext.moduleContext.imports.addAll(model.moduleContext.imports);
        }

        return traitContext;
    }

    private PathItemParseResult makeOperation(PathItemParseContext context) {
        PathItemParseResult result = new PathItemParseResult();
        result.supportingTypes = new ArrayList<>();
        List<TraitContext.Parameter> parameters = new ArrayList<>();

        // Fix up path parameters
        List<Parameter> pathParameters = context.operation.getParameters();
        if (null != pathParameters) {
            for (Parameter parameter : pathParameters) {
                parameters.add(this.makeParameter(parameter));
            }
        }

        // Fix up request body
        PathItemParseResult requestBody = makeParameterForRequestBody(context);
        if (null != requestBody.parameter) {
            parameters.add(requestBody.parameter);
        }
        if (null != requestBody.supportingTypes) {
            result.supportingTypes.addAll(requestBody.supportingTypes);
        }

        // Fix up response type
        PathItemParseResult returnType = makeReturnType(context);
        if (null != returnType.supportingTypes) {
            result.supportingTypes.addAll(returnType.supportingTypes);
        }

        boolean mutable = methodRequiresMutable(context.method);
        result.operation = new TraitContext.Operation(context.methodName, mutable, parameters, returnType.returnType);
        return result;
    }

    private static boolean methodRequiresMutable(PathItem.HttpMethod method) {
        boolean mutable = false;
        switch (method) {
            case POST, PUT, PATCH, DELETE -> mutable = true;
        }

        return mutable;
    }

    private PathItemParseResult makeParameterForRequestBody(PathItemParseContext context) {
        PathItemParseResult result = new PathItemParseResult();
        RequestBody requestBody = context.operation.getRequestBody();
        if (null == requestBody) {
            return result;
        }

        Content contents = requestBody.getContent();
        if (null == contents) {
            return result;
        }

        List<String> contentTypes = contents.keySet().stream().toList();
        if (contentTypes.size() > 1) {
            List<EnumContext.Variant> variants = new ArrayList<>();
            for (Map.Entry<String, MediaType> content : contents.entrySet()) {
                Schema contentSchema = content.getValue().getSchema();
                RustIdentifier identifier = translateResponseCodeToName(content.getKey());
                variants.add(new EnumContext.Variant(identifier,
                        new EnumContext.Variant.Type(this.modelResolver.resolveSchema(contentSchema)),
                        null, contentSchema.getDescription()));
            }

            String docComment = "Request Body supporting type for " + context.methodName + " operations on "
                    + context.traitName.toString();
            PascalCaseName typeName = new PascalCaseName(context.traitName.toString()
                    + CaseConversion.toPascalCase(context.methodName) + "RequestBody");
            ModelContext supportingType = ModelContext.forEnum(new RustType(context.path, typeName),
                    new EnumContext(variants, 0, false), docComment);
            result.supportingTypes = new ArrayList<>();
            result.supportingTypes.add(supportingType);
            result.parameter = new TraitContext.Parameter(new SnakeCaseName("body"), supportingType.rustType);
        } else {
            result.parameter = new TraitContext.Parameter(new SnakeCaseName("body"),
                    this.modelResolver.resolveSchema(contents.get(contentTypes.get(0)).getSchema()));
        }

        return result;
    }

    private PathItemParseResult makeReturnType(PathItemParseContext context) {
        PathItemParseResult result = new PathItemParseResult();

        ApiResponses responses = context.operation.getResponses();
        if (responses.size() > 1) {
            List<EnumContext.Variant> variants = new ArrayList<>();
            for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
                RustIdentifier identifier = translateResponseCodeToName(entry.getKey());
                EnumContext.Variant variant;
                Content content = entry.getValue().getContent();
                if (null == content) {
                    variant = new EnumContext.Variant(identifier, null, null, null);
                } else if (content.size() > 1) {
                    throw new RuntimeException("API responses with more than one content type are not supported!");
                } else {
                    List<String> contentTypes = content.keySet().stream().toList();
                    Schema contentSchema = content.get(contentTypes.get(0)).getSchema();
                    EnumContext.Variant.Type type;
                    if (null == contentSchema) {
                        // This might happen in the schema if the content is supposed to be inferred by the MIME type.
                        type = new EnumContext.Variant.Type(httpBody);
                    } else {
                        type = new EnumContext.Variant.Type(this.modelResolver.resolveSchema(contentSchema));
                    }

                    variant = new EnumContext.Variant(identifier, type, null, null);
                }
                variants.add(variant);
            }

            String docComment = "Response body supporting type for " + context.methodName + " requests on "
                    + context.traitName.toString();
            PascalCaseName typeName = new PascalCaseName(context.traitName.toString()
                    + CaseConversion.toPascalCase(context.methodName) + "Response");
            RustType supportingType = new RustType(CratePath.empty(), typeName);

            ModelContext supportingModel = ModelContext.forEnum(supportingType, new EnumContext(variants, 0, false), docComment);
            result.supportingTypes = new ArrayList<>();
            result.supportingTypes.add(supportingModel);

            result.returnType = new TraitContext.ReturnType(supportingType);
        } else {
            List<String> codes = responses.keySet().stream().toList();
            Content firstResponse = responses.get(codes.get(0)).getContent();
            List<String> contentTypes = firstResponse.keySet().stream().toList();
            Schema schema = firstResponse.get(contentTypes.get(0)).getSchema();
            if (null == schema) {
                return result;
            }

            result.returnType = new TraitContext.ReturnType(this.modelResolver.resolveSchema(schema));
        }

        return result;
    }

    private TraitContext.Parameter makeParameter(Parameter parameter) {
        return new TraitContext.Parameter(CaseConversion.toSnakeCase(parameter.getName()),
                this.modelResolver.resolveSchema(parameter.getSchema()));
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

    private static RustIdentifier translateResponseCodeToName(String responseCode) {
        try {
            String name;
            int value = Integer.parseInt(responseCode);
            switch (value) {
                case 200: name = "Ok"; break;
                case 201: name = "Created"; break;
                case 202: name = "Accepted"; break;
                case 204: name = "NoContent"; break;
                default: throw new RuntimeException("Unexpected response code " + value);
            }

            return new RustIdentifier(new PascalCaseName(name));
        } catch (NumberFormatException e) {
            return new RustIdentifier(RustConfig.sanitizeIdentifier(responseCode));
        }
    }

    private static class PathItemParseContext {
        public PathItemParseContext() {}

        CratePath path;
        public PascalCaseName traitName;
        public Operation operation;
        public String methodName;
        public PathItem.HttpMethod method;
    }

    private static class PathItemParseResult {
        public PathItemParseResult() {}

        public TraitContext.Parameter parameter;
        public TraitContext.ReturnType returnType;
        public TraitContext.Operation operation;
        public List<ModelContext> supportingTypes;
    }
}
