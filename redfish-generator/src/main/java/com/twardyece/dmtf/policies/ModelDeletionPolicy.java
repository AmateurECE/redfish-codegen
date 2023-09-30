package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.StructContext;
import com.twardyece.dmtf.rust.RustType;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelDeletionPolicy implements IModelGenerationPolicy {
    private final Pattern pattern;

    public ModelDeletionPolicy(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        Predicate<Map.Entry<String, ModuleFile<ModelContext>>> filter = e -> {
            Matcher matcher = this.pattern.matcher(e.getKey());
            return matcher.find();
        };
        List<String> markedModels = models.entrySet().stream().filter(filter).map(Map.Entry::getKey).toList();
        List<RustType> markedTypes = markedModels.stream().map(models::get)
                .map(m -> m.getContext().rustType).toList();
        models.values().forEach(model -> {
            ModelContext modelContext = model.getContext();
            if (null != modelContext.structContext) {
                List<StructContext.Property> markedProperties = modelContext.structContext.properties.stream()
                        .filter(property -> propertyTypeMarkedForDeletion(property, markedTypes)).toList();
                markedProperties.forEach(property -> modelContext.structContext.properties.remove(property));
            }
        });

        markedModels.forEach(models::remove);
    }

    public static boolean propertyTypeMarkedForDeletion(StructContext.Property property, List<RustType> markedTypes) {
        if (!property.getRustType().getInnerTypes().isEmpty()) {
            return markedTypes.contains(property.getRustType().getInnerTypes().get(0));
        } else {
            return markedTypes.contains(property.getRustType());
        }
    }
}
