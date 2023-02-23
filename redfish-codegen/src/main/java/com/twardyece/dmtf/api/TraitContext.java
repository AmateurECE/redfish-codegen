package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;

public class TraitContext {
    public TraitContext(CratePath path, PascalCaseName name, PathItem pathItem, List<String> mountpoints) {
        this.path = path;
        this.traitName = name;
        this.pathItem = pathItem;
        this.mountpoints = mountpoints;
        this.submodulePaths = new ArrayList<>();
    }

    public CratePath path;
    public PascalCaseName traitName;
    public PathItem pathItem;
    public List<String> mountpoints;
    public List<CratePath> submodulePaths;

    public String name() { return this.traitName.toString(); }
}
