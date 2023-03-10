package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.api.TraitContext;
import com.twardyece.dmtf.text.PascalCaseName;

import java.util.List;
import java.util.Optional;

public class PatchRequestBodyTypePolicy implements IApiGenerationPolicy {
    private static final RustType serdeJson = new RustType(CratePath.parse("serde_json"), new PascalCaseName("Value"));

    public PatchRequestBodyTypePolicy() {}

    @Override
    public void apply(List<ModuleFile<TraitContext>> traits) {
        // Translate the requestBody parameter for each trait that has a patch operation to serde_json::Value.
        for (ModuleFile<TraitContext> trait : traits) {
            Optional<TraitContext.Operation> patch = trait.getContext().operations
                    .stream()
                    .filter((o) -> "patch".equals(o.name))
                    .findFirst();

            if (patch.isPresent()) {
                TraitContext.Parameter requestBody = patch.get().parameters
                        .stream()
                        .filter((p) -> "body".equals(p.name()))
                        .findFirst()
                        .get();

                requestBody.rustType = serdeJson;
            }
        }
    }
}
