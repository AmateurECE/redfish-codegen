package com.twardyece.dmtf.model.context.factory;

import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumContextFactory implements IModelContextFactory {
    public EnumContextFactory() {}

    @Override
    public ModelContext makeModelContext(RustType rustType, Schema schema) {
        if (null == schema.getEnum()) {
            return null;
        }

        Map<String, String> docComments = new HashMap<>();
        Map<String, Map<String, String>> extensions = schema.getExtensions();
        if (extensions.containsKey("x-enumDescriptions")) {
            for (Map.Entry<String, String> value : extensions.get("x-enumDescriptions").entrySet()) {
                docComments.put(value.getKey(), value.getValue());
            }
        }

        if (extensions.containsKey("x-enumLongDescriptions")) {
            for (Map.Entry<String, String> value : extensions.get("x-enumLongDescriptions").entrySet()) {
                docComments.put(value.getKey(), value.getValue());
            }
        }

        if (extensions.containsKey("x-enumVersionAdded")) {
            for (Map.Entry<String, String> value : extensions.get("x-enumVersionAdded").entrySet()) {
                String added = "Added in version " + value.getValue() + ".";
                String existing = docComments.getOrDefault(value.getKey(), null);
                if (null != existing) {
                    docComments.put(value.getKey(), existing + " " + added);
                } else {
                    docComments.put(value.getKey(), added);
                }
            }
        }

        List<EnumContext.Variant> variants = (List<EnumContext.Variant>) schema.getEnum().stream()
                .map((s) -> makeVariant((String) s, docComments.getOrDefault(s, null)))
                .collect(Collectors.toList());
        return ModelContext.forEnum(rustType, new EnumContext(variants, true), null, schema.getDescription());
    }

    private static EnumContext.Variant makeVariant(String value, String docComment) {
        RustIdentifier name = new RustIdentifier(RustConfig.sanitizeIdentifier(value));
        String serdeName = null;
        if (!name.toString().equals(value)) {
            serdeName = value;
        }

        return new EnumContext.Variant(name, null, serdeName, docComment);
    }
}
