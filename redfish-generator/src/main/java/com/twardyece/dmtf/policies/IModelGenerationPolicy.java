package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;

import java.util.Map;

public interface IModelGenerationPolicy {
    // This interface allows the abstract creation and application of highly invasive, low-level policies and operations
    // on the set of models as a whole. These are sometimes necessary to facilitate the application of certain business
    // rules which should not be coupled to context factories.
    void apply(Map<String, ModuleFile<ModelContext>> models);
}
