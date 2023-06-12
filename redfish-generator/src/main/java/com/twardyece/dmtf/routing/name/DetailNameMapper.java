package com.twardyece.dmtf.routing.name;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailNameMapper implements INameMapper {
    private static final Pattern pattern = Pattern.compile("^\\{(?<name>[A-Za-z0-9]+)\\}$");
    public DetailNameMapper() {}

    @Override
    public SnakeCaseName matchComponent(String name) {
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        return CaseConversion.toSnakeCase(matcher.group("name").replace("Id", "Detail"));
    }
}
