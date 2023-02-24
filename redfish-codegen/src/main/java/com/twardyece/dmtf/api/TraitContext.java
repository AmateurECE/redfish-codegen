package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TraitContext {
    public TraitContext(CratePath path, PascalCaseName name, List<String> mountpoints, List<Operation> operations) {
        this.path = path;
        this.traitName = name;
        this.mountpoints = mountpoints;
        this.submodulePaths = new ArrayList<>();
        this.operations = operations;
    }

    public final CratePath path;
    public final PascalCaseName traitName;
    public final List<String> mountpoints;
    public final List<SnakeCaseName> submodulePaths;
    public final List<Operation> operations;

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

    public static class Operation {
        public Operation(PathItem.HttpMethod method, List<Parameter> parameters, ReturnType returnType) {
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
            this.parameters = parameters;
            this.returnType = returnType;
        }

        public String name;
        public List<Parameter> parameters;
        public ReturnType returnType;
    }

    public static class ReturnType {
        public ReturnType(RustType rustType) { this.rustType = rustType; }

        public RustType rustType;

        public String type() { return this.rustType.toString(); }
    }

    public static class Parameter {
        public Parameter(SnakeCaseName parameterName, RustType rustType) {
            this.parameterName = parameterName;
            this.rustType = rustType;
        }

        public SnakeCaseName parameterName;
        public RustType rustType;

        public String name() {
            return this.parameterName.toString();
        }

        public String type() {
            return this.rustType.toString();
        }
    }
}
