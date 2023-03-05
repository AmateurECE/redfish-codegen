package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.RustType;

import java.util.ArrayList;
import java.util.List;

public class TupleContext {
    public RustType rustType;

    public TupleContext(RustType rustType) { this.rustType = rustType; }

    public List<RustType> getDependentTypes() {
        List<RustType> types = new ArrayList<>();
        types.add(rustType);
        return types;
    }

    public String type() { return this.rustType.toString(); }
}
