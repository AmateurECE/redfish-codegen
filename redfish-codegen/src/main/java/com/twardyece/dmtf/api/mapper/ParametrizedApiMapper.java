package com.twardyece.dmtf.api.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedApiMapper implements IApiFileMapper {
    private static String prefix = "/redfish/v1/";
    private static Pattern pattern = Pattern.compile("(?<=\\.)([A-Za-z0-9]+)|(?<=\\{)([A-Za-z0-9]+)(?=\\})|([A-Za-z0-9]+)$");

    public ApiMatchResult matches(String path) {
        String[] components = path.substring(prefix.length()).split("/");
        List<SnakeCaseName> module = new ArrayList<>();
        for (String component : components) {
            Matcher matcher = pattern.matcher(component);
            if (matcher.find()) {
                if (null != matcher.group(1)) { // An action
                    module.add(CaseConversion.toSnakeCase(matcher.group(1)));
                } else if (null != matcher.group(2)) {
                    // Intentionally ignore case two, which matches on path parameters
                } else if (null != matcher.group(3)) { // A PascalCase identifier
                    module.add(CaseConversion.toSnakeCase(matcher.group(3)));
                }
            } else {
                return null;
            }
        }

        return new ApiMatchResult(module);
    }
}
