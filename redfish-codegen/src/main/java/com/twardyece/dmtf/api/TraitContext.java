package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<SnakeCaseName> submodulePaths;

    public String name() { return this.traitName.toString(); }
    public List<Submodule> submodules() {
        return this.submodulePaths
                .stream()
                .map((p) -> new Submodule(p.toString()))
                .collect(Collectors.toList());
    }

    public class Submodule {
        public Submodule(String name) {
            this.name = name;
        }

        public String name;
    }
}
