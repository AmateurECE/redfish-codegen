package com.twardyece.dmtf.specification;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleModelIdentifierFactory {
    private final Pattern pattern;
    private final String modelGroupName;

    public SimpleModelIdentifierFactory(Pattern pattern, String modelGroupName) {
        this.pattern = pattern;
        this.modelGroupName = modelGroupName;
    }

    public Optional<PascalCaseName> identify(String identifier) {
        Matcher matcher = this.pattern.matcher(identifier);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(CaseConversion.toPascalCase(matcher.group(this.modelGroupName)));
    }
}
