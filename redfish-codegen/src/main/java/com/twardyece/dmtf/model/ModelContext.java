package com.twardyece.dmtf.model;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;

public class ModelContext {
    PascalCaseName modelName;
    SnakeCaseName modelModule;
    StructContext struct;
    List<Import> imports;

    public static ModelContext struct(PascalCaseName modelName, SnakeCaseName modelModule, StructContext struct, List<Import> imports) {
        ModelContext modelContext = new ModelContext();
        modelContext.modelName = modelName;
        modelContext.modelModule = modelModule;
        modelContext.struct = struct;
        modelContext.imports = imports;
        return modelContext;
    }

    public String name() { return this.modelName.toString(); }

    static class Import {
        Import(CratePath cratePath) {
            this.cratePath = cratePath;
        }

        CratePath cratePath;

        public String path() { return this.cratePath.toString(); }
    }
}
