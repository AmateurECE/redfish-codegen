package com.twardyece.dmtf.api;

import io.swagger.annotations.ApiResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathNameTranslator {
    private Pattern schemaRefPattern = Pattern.compile("^(https.*yaml)?#/components/schemas/(?<name>.*)$");
    public PathNameTranslator() {}

    public String translate(String path, PathItem pathItem) {
        String name = "";
        Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();
        if (operations.containsKey(PathItem.HttpMethod.GET)) {
            ApiResponses responses = operations.get(PathItem.HttpMethod.GET).getResponses();
            List<String> codes = operations.get(PathItem.HttpMethod.GET).getResponses().keySet().stream().toList();
            Content firstResponse = responses.get(codes.get(0)).getContent();
            List<String> contentTypes = firstResponse.keySet().stream().toList();
            Schema schema = firstResponse.get(contentTypes.get(0)).getSchema();
            if (null != schema) {
                Matcher matcher = schemaRefPattern.matcher(schema.get$ref());
                matcher.find();
                name += "get#" + matcher.group("name") + ";";
            }
        }
        if (operations.containsKey(PathItem.HttpMethod.POST)) {
            String body = operations.get(PathItem.HttpMethod.POST).getRequestBody().get$ref();
            name += "post#" + body + ";";
        }

        if ("".equals(name)) {
            return path;
        } else {
            return name;
        }
    }
}
