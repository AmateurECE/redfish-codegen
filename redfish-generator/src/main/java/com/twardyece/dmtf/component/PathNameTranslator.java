package com.twardyece.dmtf.component;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;

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
            Content content = operations.get(PathItem.HttpMethod.POST).getRequestBody().getContent();
            List<String> contentTypes = content.keySet().stream().toList();
            Schema schema = content.get(contentTypes.get(0)).getSchema();
            if (null != schema) {
                Matcher matcher = schemaRefPattern.matcher(schema.get$ref());
                matcher.find();
                name += "post#" + matcher.group("name") + ";";
            }
        }

        if ("".equals(name)) {
            String[] components = path.split("/");
            return components[components.length - 1] + ";";
        } else {
            return name;
        }
    }
}
