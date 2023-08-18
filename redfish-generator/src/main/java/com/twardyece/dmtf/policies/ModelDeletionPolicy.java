package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelDeletionPolicy implements IModelGenerationPolicy {
    private final Pattern modelDeletionPattern;

    public ModelDeletionPolicy(Pattern modelDeletionPattern) {
        this.modelDeletionPattern = modelDeletionPattern;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        List<String> deletedModels = new ArrayList<>();
        for (String model : models.keySet()) {
            Matcher matcher = modelDeletionPattern.matcher(model);
            if (matcher.find()) {
                deletedModels.add(model);
            }
        }

        deletedModels.forEach(models::remove);
    }
}
