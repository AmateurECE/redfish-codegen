package com.twardyece.dmtf.api;

import io.swagger.v3.oas.models.PathItem;
import org.checkerframework.checker.units.qual.A;

import java.util.Collection;
import java.util.List;

class ApiEndpoint implements Comparable<ApiEndpoint> {
    public ApiEndpoint(String name, String path, PathItem pathItem) {
        this.name = name;
        this.path = path;
        this.pathItem = pathItem;
    }

    private String name;
    private String path;
    private PathItem pathItem;
    private List<String> validMountpoints;

    public boolean isEndpoint() { return null != this.pathItem; }
    public String getName() { return this.name; }
    public String getPath() { return this.path; }
    public PathItem getPathItem() { return this.pathItem; }
    public void setValidMountpoints(List<String> mountpoints) { this.validMountpoints = mountpoints; }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(ApiEndpoint o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ApiEndpoint) {
            return this.name.equals(((ApiEndpoint) o).name);
        } else {
            return false;
        }
    }
}
