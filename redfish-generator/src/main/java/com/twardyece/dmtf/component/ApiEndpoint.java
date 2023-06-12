package com.twardyece.dmtf.component;

import io.swagger.v3.oas.models.PathItem;

class ApiEndpoint implements Comparable<ApiEndpoint> {
    public ApiEndpoint(String name, String summary, PathItem pathItem) {
        this.name = name;
        this.summary = summary;
        this.pathItem = pathItem;
    }

    private String name;
    private String summary;
    private PathItem pathItem;

    public String getName() { return this.name; }
    public String getSummary() { return this.summary; }
    public PathItem getPathItem() { return this.pathItem; }

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
