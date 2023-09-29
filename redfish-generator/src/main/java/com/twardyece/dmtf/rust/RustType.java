package com.twardyece.dmtf.rust;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RustType implements Comparable<RustType> {
    // The absolute path of the type (i.e., where its definition lives)
    private final CratePath path;
    // The path of the type, taking into consideration any namespaces which are importing with "use" statements
    private ICaseConvertible name;
    private final List<RustType> innerTypes;

    public RustType(SnakeCaseName name) {
        this.name = name;
        this.path = CratePath.empty();
        this.innerTypes = new ArrayList<>();
    }

    public RustType(CratePath path, PascalCaseName name) {
        this.path = Objects.requireNonNull(path);
        this.name = name;
        this.innerTypes = new ArrayList<>();
    }

    public RustType(CratePath path, PascalCaseName name, RustType[] innerTypes) {
        this.path = Objects.requireNonNull(path);
        this.name = name;
        this.innerTypes = Arrays.stream(innerTypes).toList();
    }

    @Override
    public String toString() {
        String value = this.path.joinComponent(this.name);
        if (!this.innerTypes.isEmpty()) {
                value += "<" + String.join(",", this.innerTypes.stream().map(RustType::toString).toList()) + ">";
        }

        return value;
    }

    public CratePath getPath() { return this.path; }

    // A type is primitive if it does not require importing its containing module.
    public boolean isPrimitive() { return this.path.isEmpty(); }

    public List<RustType> getInnerTypes() { return this.innerTypes; }

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
