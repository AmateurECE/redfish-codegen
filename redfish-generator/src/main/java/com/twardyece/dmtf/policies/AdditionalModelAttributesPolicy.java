package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.model.context.StructContext;
import com.twardyece.dmtf.model.context.TupleContext;
import com.twardyece.dmtf.rust.ToRustExpression;
import com.twardyece.dmtf.rust.RustType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AdditionalModelAttributesPolicy implements IModelGenerationPolicy {
    private final Pattern modelPattern;
    private final ToRustExpression.RustExpression attribute;
    private final ModelResolver modelResolver;
    private final boolean recursivelyApplyToProperties;

    public AdditionalModelAttributesPolicy(Pattern modelPattern, ToRustExpression.RustExpression attribute,
                                           ModelResolver modelResolver, boolean recursivelyApplyToProperties) {
        this.modelPattern = modelPattern;
        this.attribute = attribute;
        this.modelResolver = modelResolver;
        this.recursivelyApplyToProperties = recursivelyApplyToProperties;
    }

    @Override
    public void apply(Map<String, ModuleFile<ModelContext>> models) {
        for (Map.Entry<String, ModuleFile<ModelContext>> model : models.entrySet()) {
            Matcher matcher = modelPattern.matcher(model.getKey());
            if (matcher.find()) {
                ModelContext modelContext = model.getValue().getContext();
                if (recursivelyApplyToProperties) {
                    applyToProperties(models, modelContext);
                }
            }
        }
    }

    private void applyToProperties(Map<String, ModuleFile<ModelContext>> models, ModelContext modelContext) {
        modelContext.additionalAttributes.add(this.attribute);
        List<RustType> propertyTypes;
        if (null != modelContext.structContext) {
            propertyTypes = getStructPropertyTypes(modelContext.structContext);
        } else if (null != modelContext.tupleContext) {
            propertyTypes = getTuplePropertyTypes(modelContext.tupleContext);
        } else if (null != modelContext.enumContext) {
            propertyTypes = getEnumPropertyTypes(modelContext.enumContext);
        } else {
            return;
        }

        propertyTypes.forEach(type -> {
            String modelName = this.modelResolver.reverseResolveIdentifier(type);
            ModelContext context = models.get(modelName).getContext();
            applyToProperties(models, context);
        });
    }

    private static List<RustType> getStructPropertyTypes(StructContext context) {
        return context.properties.stream()
                .flatMap(property -> getPropertyNonPrimitiveTypes(property.getRustType()))
                .toList();
    }

    private static List<RustType> getTuplePropertyTypes(TupleContext context) {
        return new ArrayList<>(getPropertyNonPrimitiveTypes(context.rustType).toList());
    }

    private static List<RustType> getEnumPropertyTypes(EnumContext context) {
        return context.variants.stream()
                .filter(variant -> null != variant.type)
                .flatMap(variant -> getPropertyNonPrimitiveTypes(variant.type.rustType))
                .toList();
    }

    private static Stream<RustType> getPropertyNonPrimitiveTypes(RustType rustType) {
        if (!rustType.getInnerTypes().isEmpty()) {
            return rustType.getInnerTypes().stream().filter(rustType1 -> !rustType1.isPrimitive());
        } else if (rustType.getPath().isCrateLocal()) {
            return Stream.of(rustType);
        } else {
            return Stream.empty();
        }
    }
}
