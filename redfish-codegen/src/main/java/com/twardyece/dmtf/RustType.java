package com.twardyece.dmtf;

import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RustType {
    private CratePath path;
    private ICaseConvertible name;
    private RustType innerType;

    public RustType(SnakeCaseName name) {
        this.name = name;
    }

    public RustType(CratePath path, PascalCaseName name) {
        this.path = path;
        this.name = name;
    }

    public RustType(CratePath path, PascalCaseName name, RustType innerType) {
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

    public CratePath getPath() { return this.path; }

    // A type is primitive if it does not require importing its containing module.
    public boolean isPrimitive() { return null == this.path; }
}
