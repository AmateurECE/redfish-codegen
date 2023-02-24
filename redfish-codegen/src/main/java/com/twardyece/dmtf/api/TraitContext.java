package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public List<Operation> operations() {
        Map<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> operations = this.pathItem.readOperationsMap();
        return operations.entrySet()
                .stream()
                .map((e) -> new TraitContext.Operation(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public class Submodule {
        public Submodule(String name) {
            this.name = name;
        }

        public String name;
    }

    public class Operation {
        public Operation(PathItem.HttpMethod method, io.swagger.v3.oas.models.Operation operation) {
            switch (method) {
                case POST -> this.name = "post";
                case GET -> this.name = "get";
                case PUT -> this.name = "put";
                case PATCH -> this.name = "patch";
                case DELETE -> this.name = "delete";
                case HEAD -> this.name = "head";
                case OPTIONS -> this.name = "options";
                case TRACE -> this.name = "trace";
            }
            this.returnType = null;
            this.parameters = null;
        }

        public String name;
        public List<Parameter> parameters;
        public ReturnType returnType;
    }

    public class ReturnType {
        public ReturnType(String name) { this.name = name; }

        public String name;
    }

    public class Parameter {
        public Parameter() {}
    }
}
