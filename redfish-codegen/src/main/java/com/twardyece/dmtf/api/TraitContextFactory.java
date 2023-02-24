package com.twardyece.dmtf.api;

import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Map;

public class TraitContextFactory {
    private final ModelResolver modelResolver;
    private final EndpointResolver endpointResolver;
    private final Map<PascalCaseName, PascalCaseName> traitNameOverrides;
    public TraitContextFactory(ModelResolver modelResolver, EndpointResolver endpointResolver, Map<PascalCaseName, PascalCaseName> traitNameOverrides) {
        this.modelResolver = modelResolver;
        this.endpointResolver = endpointResolver;
        this.traitNameOverrides = traitNameOverrides;
    }

    public TraitContext makeTraitContext(List<String> path, PathItem pathItem, List<String> mountpoints) {
        EndpointResolver.ApiMatchResult result = endpointResolver.resolve(path);
        if (traitNameOverrides.containsKey(result.name)) {
            result.name = traitNameOverrides.get(result.name);
        }

        return new TraitContext(result.path, result.name, pathItem, mountpoints);
    }
}
