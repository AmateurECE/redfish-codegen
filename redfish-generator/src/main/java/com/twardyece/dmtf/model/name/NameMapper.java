package com.twardyece.dmtf.model.name;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameMapper implements INameMapper {
    private final Pattern pattern;
    private final String groupName;

    public NameMapper(Pattern pattern, String groupName) {
        this.pattern = pattern;
        this.groupName = groupName;
    }

    @Override
    public SnakeCaseName matchComponent(String name) {
        Matcher matcher = this.pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        return CaseConversion.toSnakeCase(matcher.group(this.groupName));
    }
}
