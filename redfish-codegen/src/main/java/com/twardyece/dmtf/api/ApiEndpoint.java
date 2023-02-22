package com.twardyece.dmtf.api;

import io.swagger.v3.oas.models.PathItem;
import org.checkerframework.checker.units.qual.A;

import java.util.Collection;
import java.util.List;

class ApiEndpoint implements Comparable<ApiEndpoint> {
    public static ApiEndpoint component(String name) {
        ApiEndpoint endpoint = new ApiEndpoint();
        endpoint.name = name;
        return endpoint;
    }
    public static ApiEndpoint root() { return ApiEndpoint.component(""); }
    public static ApiEndpoint endpoint(String name, PathItem pathItem) {
        ApiEndpoint endpoint = ApiEndpoint.component(name);
        endpoint.pathItem = pathItem;
        return endpoint;
    }

    private String name;
    private PathItem pathItem;
    private List<String> validMountpoints;

    public boolean isEndpoint() { return null != this.pathItem; }
    public String getName() { return this.name; }
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
