package com.twardyece.dmtf.model;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;

public class ModelContext {
    PascalCaseName modelName;
    SnakeCaseName modelModule;
    StructContext structContext;
    EnumContext enumContext;
    List<Import> imports;
    String docComment;

    private static ModelContext generic(PascalCaseName modelName, SnakeCaseName modelModule, List<Import> imports,
                                        String docComment) {
        ModelContext modelContext = new ModelContext();
        modelContext.modelName = modelName;
        modelContext.modelModule = modelModule;
        modelContext.imports = imports;
        modelContext.docComment = docComment;
        return modelContext;
    }

    public static ModelContext forStruct(PascalCaseName modelName, SnakeCaseName modelModule, StructContext structContext,
                                         List<Import> imports, String docComment) {
        ModelContext modelContext = ModelContext.generic(modelName, modelModule, imports, docComment);
        modelContext.structContext = structContext;
        return modelContext;
    }

    public static ModelContext forEnum(PascalCaseName modelName, SnakeCaseName modelModule, EnumContext enumContext,
                                       List<Import> imports, String docComment) {
        ModelContext modelContext = ModelContext.generic(modelName, modelModule, imports, docComment);
        modelContext.enumContext = enumContext;
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
