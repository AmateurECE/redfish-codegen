package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.api.TraitContext;

import java.util.List;

public interface IApiGenerationPolicy {
    void apply(List<ModuleFile<TraitContext>> traits);
}
