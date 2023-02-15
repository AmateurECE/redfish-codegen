package com.twardyece.dmtf;

import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RustType {
    // The absolute path of the type (i.e., where its definition lives)
    private CratePath path;
    // The path of the type, taking into consideration any namespaces which are importing with "use" statements
    private CratePath importPath;
    private ICaseConvertible name;
    private RustType innerType;

    public RustType(SnakeCaseName name) {
        this.name = name;
    }

    public RustType(CratePath path, PascalCaseName name) {
        this.path = path;
        this.importPath = path;
        this.name = name;
    }

    public RustType(CratePath path, PascalCaseName name, RustType innerType) {
        this.path = path;
        this.importPath = path;
        this.name = name;
        this.innerType = innerType;
    }

    @Override
    public String toString() {
        if (null != this.innerType) {
            return this.name.toString() + "<" + this.innerType.getPath().joinType(this.innerType) + ">";
        } else {
            return this.name.toString();
        }
    }

    public CratePath getPath() { return this.path; }

    // A type is primitive if it does not require importing its containing module.
    public boolean isPrimitive() { return null == this.path; }

    public void setImportPath(CratePath path) { this.importPath = path; }

    public RustType getInnerType() { return this.innerType; }
}
