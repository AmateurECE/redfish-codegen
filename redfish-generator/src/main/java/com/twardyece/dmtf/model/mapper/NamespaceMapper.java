package com.twardyece.dmtf.model.mapper;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceMapper implements IModelModelMapper {
    private final Pattern namespace;
    private final String replacement;

    public NamespaceMapper(Pattern namespace, String replacement) {
        this.namespace = namespace;
        this.replacement = replacement;
    }

    @Override
    public Optional<String> match(String model) {
        Matcher matcher = namespace.matcher(model);
        if (matcher.find()) {
            matcher.reset();
            return Optional.of(matcher.replaceAll(replacement));
        } else {
            return Optional.empty();
        }
    }
}
