package com.twardyece.dmtf.model.context;

import com.twardyece.dmtf.rust.RustType;

public class TupleContext {
    public RustType rustType;

    public TupleContext(RustType rustType) { this.rustType = rustType; }

    public String type() { return this.rustType.toString(); }
}
