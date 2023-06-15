package com.twardyece.dmtf.component;

import com.twardyece.dmtf.RustType;
import io.swagger.v3.oas.models.PathItem;

class ApiComponent implements Comparable<ApiComponent> {
    public ApiComponent(RustType rustType, PathItem pathItem) {
        this.rustType = rustType;
        this.pathItem = pathItem;
    }

    public final RustType rustType;
    public final PathItem pathItem;

    @Override
    public String toString() {
        return this.rustType.toString();
    }

    @Override
    public int compareTo(ApiComponent o) {
        return this.rustType.compareTo(o.rustType);
    }

    @Override
    public int hashCode() {
        return this.rustType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ApiComponent) {
            return this.rustType.equals(((ApiComponent) o).rustType);
        } else {
            return false;
        }
    }
}
