package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.StructContext;
import com.twardyece.dmtf.rust.RustType;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class OemModelDeletionPolicy implements IModelGenerationPolicy {
    public OemModelDeletionPolicy() {}

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        Predicate<Map.Entry<String, ModuleFile<ModelContext>>> oemFilter = e -> {
            String name = e.getValue().getContext().rustType.getName().toString();
            return name.equals("Oem") || name.equals("OemActions");
        };
        List<String> oemModels = models.entrySet().stream().filter(oemFilter).map(Map.Entry::getKey).toList();
        List<RustType> oemTypes = models.entrySet().stream().filter(oemFilter)
                .map(e -> e.getValue().getContext().rustType).toList();
        models.values().forEach(model -> {
            ModelContext modelContext = model.getContext();
            if (null != modelContext.structContext) {
                List<StructContext.Property> oemProperties = modelContext.structContext.properties.stream()
                        .filter(property -> isOemProperty(property, oemTypes)).toList();
                oemProperties.forEach(property -> modelContext.structContext.properties.remove(property));
            }
        });

        oemModels.forEach(models::remove);
    }

    public static boolean isOemProperty(StructContext.Property property, List<RustType> oemTypes) {
        if (!property.getRustType().getInnerTypes().isEmpty()) {
            return oemTypes.contains(property.getRustType().getInnerTypes().get(0));
        } else {
            return oemTypes.contains(property.getRustType());
        }
    }
}
