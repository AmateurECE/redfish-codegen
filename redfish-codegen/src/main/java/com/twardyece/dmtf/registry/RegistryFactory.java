package com.twardyece.dmtf.registry;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
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
        return new RegistryContext.Variant(new PascalCaseName(name));
    }
}
