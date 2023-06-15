package com.twardyece.dmtf.component.match;

import io.swagger.v3.oas.models.PathItem;

import java.util.Optional;

public interface IComponentMatcher {
    Optional<String> getComponent(String uri, PathItem pathItem);
}
