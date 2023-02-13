package com.twardyece.dmtf;

import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

public class RustType {
    private SnakeCaseName[] path;
    private ICaseConvertible name;
    private RustType innerType;

    public RustType(SnakeCaseName name) {
        this.name = name;
    }

    public RustType(SnakeCaseName[] path, PascalCaseName name) {
        this.path = path;
        this.name = name;
    }

    public RustType(SnakeCaseName[] path, PascalCaseName name, RustType innerType) {
        this.path = path;
        this.name = name;
        this.innerType = innerType;
    }

    @Override
    public String toString() {
        if (null != this.innerType) {
            return this.name.toString() + "<" + this.innerType.toString() + ">";
        } else {
            return this.name.toString();
        }
    }
}
