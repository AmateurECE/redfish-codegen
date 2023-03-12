package com.twardyece.dmtf.registry;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.PascalCaseName;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistryFactory {
    private final RustType messageType;
    private final RustType health;
    public RegistryFactory(RustType messageType, RustType health) {
        this.messageType = messageType;
        this.health = health;
    }

    public RegistryContext makeRegistry(RustType rustType, Path registryFile) throws IOException {
        JSONObject object = new JSONObject(Files.readString(registryFile));
        JSONObject messages = object.getJSONObject("Messages");
        String idBase = object.getString("Id");
        List<RegistryContext.Variant> variants = new ArrayList<>();
        for (String key : messages.keySet()) {
            variants.add(makeVariant(key, messages.getJSONObject(key), idBase));
        }

        return new RegistryContext(new ModuleContext(rustType.getPath(), null), rustType,
                object.getString("Description"), this.messageType, variants);
    }

    private RegistryContext.Variant makeVariant(String identifier, JSONObject object, String idBase) {
        PascalCaseName name = new PascalCaseName(identifier);
        List<RegistryContext.Variant.Field> innerFields = null;
        if (object.keySet().contains("ParamTypes")) {
            JSONArray params = object.getJSONArray("ParamTypes");
            JSONArray descriptions = null;
            if (object.keySet().contains("ArgLongDescriptions")) {
                descriptions = object.getJSONArray("ArgLongDescriptions");
            }
            innerFields = new ArrayList<>();
            for (int i = 0; i < params.length(); ++i) {
                RustType type = ModelResolver.RUST_TYPE_MAP.get(params.getString(i));
                String docComment = null;
                if (null != descriptions) {
                    docComment = descriptions.getString(i);
                }
                innerFields.add(new RegistryContext.Variant.Field(type, docComment));
            }
        }

        RustIdentifier severity = new RustIdentifier(this.health,
                new PascalCaseName(object.getString("MessageSeverity")));
        String id = idBase + "." + name;
        return new RegistryContext.Variant(name, object.getString("LongDescription"),
                object.getString("Message"), id, severity, object.getString("Resolution"), innerFields);
    }
}
