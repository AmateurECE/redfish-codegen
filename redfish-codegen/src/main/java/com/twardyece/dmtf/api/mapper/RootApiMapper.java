package com.twardyece.dmtf.api.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RootApiMapper implements IApiFileMapper {
    private static final Pattern pattern = Pattern.compile("/redfish/v1/?$");

    public ApiMatchResult matches(String path) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            List<SnakeCaseName> module = new ArrayList<>();
            module.add(new SnakeCaseName("service_root"));
            return new ApiMatchResult(module);
        } else {
            return null;
        }
    }
}
