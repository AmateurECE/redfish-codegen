package com.twardyece.dmtf.api.mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopApiMapper implements IApiFileMapper {
    private static final Pattern pattern = Pattern.compile("/redfish/v1/([$A-Za-z0-9]*)$");
    @Override
    public ApiMatchResult matches(String name) {
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            // TODO?
            return null;
        } else {
            return null;
        }
    }
}
