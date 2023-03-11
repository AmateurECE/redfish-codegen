package com.twardyece.dmtf;

import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

public class RustType implements Comparable<RustType> {
    // The absolute path of the type (i.e., where its definition lives)
    private CratePath path;
    // The path of the type, taking into consideration any namespaces which are importing with "use" statements
    // TODO: Removing importPath will make this class much simpler.
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
        String value;
        if (null != this.importPath) {
            value = this.importPath.joinComponent(this.name);
        } else {
            value = this.name.toString();
        }
        if (null != this.innerType) {
                value += "<" + this.innerType + ">";
        }

        return value;
    }

    public CratePath getPath() { return this.path; }
    public void setPath(CratePath cratePath) { this.path = cratePath; }

    // A type is primitive if it does not require importing its containing module.
    public boolean isPrimitive() { return null == this.path; }

    public void setImportPath(CratePath path) { this.importPath = path; }

    public RustType getInnerType() { return this.innerType; }

    public ICaseConvertible getName() { return this.name; }
    public void setName(ICaseConvertible name) { this.name = name; }

    @Override
    public int compareTo(RustType o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RustType) {
            return this.toString().equals(o.toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
