package com.twardyece.dmtf.api.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedApiMapper implements IApiFileMapper {
    private static Pattern pattern = Pattern.compile("(?<=/)([$_A-Za-z0-9]+)(?=/?$)|([A-Za-z0-9]*)(?=/\\{[A-Za-z0-9]*\\})");

    public ApiMatchResult matches(String path) {
        Matcher matcher = pattern.matcher(path);
        List<SnakeCaseName> module = new ArrayList<>();
        while (matcher.find()) {
            if (null != matcher.group(2)) {
                module.add(CaseConversion.toSnakeCase(matcher.group(2)));
            } else if (null != matcher.group(1)) {
                module.add(CaseConversion.toSnakeCase(matcher.group(1)));
            }
        }

        return new ApiMatchResult(module);
    }
}
