package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;

import java.util.List;

public class ModelContext {
    public RustType rustType;
    public StructContext structContext;
    public EnumContext enumContext;
    public TupleContext tupleContext;
    public List<Import> imports;
    public String docComment;

    private static ModelContext generic(RustType rustType, List<Import> imports, String docComment) {
        ModelContext modelContext = new ModelContext();
        modelContext.rustType = rustType;
        modelContext.imports = imports;
        modelContext.docComment = docComment;
        return modelContext;
    }

    public static ModelContext forStruct(RustType rustType, StructContext structContext, List<Import> imports,
                                         String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, imports, docComment);
        modelContext.structContext = structContext;
        return modelContext;
    }

    public static ModelContext forEnum(RustType rustType, EnumContext enumContext, List<Import> imports,
                                       String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, imports, docComment);
        modelContext.enumContext = enumContext;
        return modelContext;
    }

    public static ModelContext forTuple(RustType rustType, TupleContext tupleContext, List<Import> imports,
                                        String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, imports, docComment);
        modelContext.tupleContext = tupleContext;
        return modelContext;
    }

    public String name() { return this.rustType.getName().toString(); }

    public static class Import {
        public Import(CratePath cratePath) {
            this.cratePath = cratePath;
        }

        CratePath cratePath;

        public String path() { return this.cratePath.toString(); }
    }
}
