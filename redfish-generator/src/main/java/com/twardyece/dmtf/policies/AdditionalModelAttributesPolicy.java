package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.rust.IRustExpression;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdditionalModelAttributesPolicy implements IModelGenerationPolicy {
    private final Pattern modelPattern;
    private final IRustExpression attribute;

    public AdditionalModelAttributesPolicy(Pattern modelPattern, IRustExpression attribute) {
        this.modelPattern = modelPattern;
        this.attribute = attribute;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        for (Map.Entry<String, ModuleFile<ModelContext>> model : models.entrySet()) {
            Matcher matcher = modelPattern.matcher(model.getKey());
            if (matcher.find()) {
                model.getValue().getContext().additionalAttributes.add(this.attribute);
            }
        }
    }
}
