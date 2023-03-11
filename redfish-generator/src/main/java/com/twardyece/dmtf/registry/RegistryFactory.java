package com.twardyece.dmtf.registry;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RegistryFactory {
    public RegistryFactory() {}

    public RegistryContext makeRegistry(RustType rustType, Path registryFile) throws IOException {
        JSONObject object = new JSONObject(Files.readString(registryFile));
        JSONObject messages = object.getJSONObject("Messages");
        List<RegistryContext.Variant> variants = new ArrayList<>();
        for (String key : messages.keySet()) {
            variants.add(makeVariant(key, messages.getJSONObject(key)));
        }

        return new RegistryContext(new ModuleContext(rustType.getPath(), null), rustType,
                object.getString("Description"), variants);
    }

    private RegistryContext.Variant makeVariant(String name, JSONObject object) {
        List<RegistryContext.Variant.Type> innerTypes = null;
        if (object.keySet().contains("ParamTypes")) {
            JSONArray params = object.getJSONArray("ParamTypes");
            JSONArray descriptions = null;
            if (object.keySet().contains("ArgLongDescriptions")) {
                descriptions = object.getJSONArray("ArgLongDescriptions");
            }
            innerTypes = new ArrayList<>();
            for (int i = 0; i < params.length(); ++i) {
                RustType type = ModelResolver.RUST_TYPE_MAP.get(params.getString(i));
                String docComment = null;
                if (null != descriptions) {
                    docComment = descriptions.getString(i);
                }
                innerTypes.add(new RegistryContext.Variant.Type(type, docComment));
            }
        }
        return new RegistryContext.Variant(new PascalCaseName(name), object.getString("LongDescription"), innerTypes);
    }
}
