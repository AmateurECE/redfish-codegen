package com.twardyece.dmtf.component.match;

import com.twardyece.dmtf.component.ComponentContext;
import com.twardyece.dmtf.component.ComponentRepository;
import io.swagger.v3.oas.models.PathItem;

import java.util.Optional;

public interface IComponentMatcher {
    Optional<ComponentContext> matchUri(ComponentRepository repository, String uri, PathItem pathItem);
}
