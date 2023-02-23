package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;

public class TraitContext {
    public TraitContext(CratePath path, PascalCaseName name, PathItem pathItem, List<String> mountpoints) {
        this.path = path;
        this.name = name;
        this.pathItem = pathItem;
        this.mountpoints = mountpoints;
        this.submodulePaths = new ArrayList<>();
    }

    CratePath path;
    PascalCaseName name;
    PathItem pathItem;
    List<String> mountpoints;
    List<CratePath> submodulePaths;

    public void addSubmodule(CratePath path) {
        this.submodulePaths.add(path);
    }
}
