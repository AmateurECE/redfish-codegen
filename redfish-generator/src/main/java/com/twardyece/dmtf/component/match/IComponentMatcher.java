package com.twardyece.dmtf.component.match;

import java.util.Optional;

public interface IComponentMatcher {
    Optional<String> getComponent(String uri);
}
