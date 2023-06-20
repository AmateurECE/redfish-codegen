package com.twardyece.dmtf.component.match;

import com.twardyece.dmtf.component.ComponentContext;
import com.twardyece.dmtf.component.ComponentRepository;
import io.swagger.v3.oas.models.PathItem;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionComponentMatcher implements IComponentMatcher {

    Pattern pattern = Pattern.compile("/Actions/(?<component>[A-Za-z0-9]*).(?<action>[A-Za-z0-9]*)");

    @Override
    public Optional<ComponentContext> matchUri(ComponentRepository repository, String uri, PathItem pathItem) {
        Matcher matcher = pattern.matcher(uri);
        if (!matcher.find()) {
            return Optional.empty();
        }

        ComponentContext context = repository.getComponentParentOfPath(uri);
        // TODO: Add actions here
        return Optional.of(context);
    }
}
