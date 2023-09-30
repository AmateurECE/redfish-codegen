package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.rust.ToRustExpression;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelContext {
    public ModuleContext moduleContext;
    public RustType rustType;
    public StructContext structContext;
    public EnumContext enumContext;
    public TupleContext tupleContext;
    public boolean unitContext;
    public Set<ToRustExpression.RustExpression> additionalAttributes;
    public String docComment;
    public Metadata metadata;

    private static ModelContext generic(RustType rustType, String docComment) {
        ModelContext modelContext = new ModelContext();
        CratePath path = rustType.getPath().append(new SnakeCaseName(rustType.getName()));
        modelContext.moduleContext = new ModuleContext(path);

        modelContext.rustType = rustType;
        modelContext.unitContext = false;
        modelContext.additionalAttributes = new HashSet<>();
        modelContext.docComment = docComment;
        return modelContext;
    }

    public static ModelContext forStruct(RustType rustType, StructContext structContext, String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, docComment);
        modelContext.structContext = structContext;
        return modelContext;
    }

    public static ModelContext forEnum(RustType rustType, EnumContext enumContext, String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, docComment);
        modelContext.enumContext = enumContext;
        return modelContext;
    }

    public static ModelContext forTuple(RustType rustType, TupleContext tupleContext, String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, docComment);
        modelContext.tupleContext = tupleContext;
        return modelContext;
    }

    public static ModelContext forUnit(RustType rustType, String docComment) {
        ModelContext modelContext = ModelContext.generic(rustType, docComment);
        modelContext.unitContext = true;
        return modelContext;
    }

    public String name() { return this.rustType.getName().toString(); }
}
