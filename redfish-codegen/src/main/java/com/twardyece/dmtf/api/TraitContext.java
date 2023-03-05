package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.EnumContext;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.LinkedList;
import java.util.List;

public class TraitContext {
    public TraitContext(CratePath path, PascalCaseName name, List<ModelContext> supportingTypes, List<String> mountpoints,
                        List<Operation> operations) {
        List<RustType> dependentTypes = new LinkedList<>();
        for (Operation operation : operations) {
            dependentTypes.addAll(operation.getDependentTypes());
        }
        this.moduleContext = new ModuleContext(path, dependentTypes);
        this.traitName = name;
        this.supportingTypes = supportingTypes;
        this.mountpoints = mountpoints;
        this.operations = operations;
    }

    public final ModuleContext moduleContext;
    public final PascalCaseName traitName;
    public final List<ModelContext> supportingTypes;
    public final List<String> mountpoints;
    public final List<Operation> operations;

    public String name() { return this.traitName.toString(); }

    public static class Operation {
        public Operation(String name, List<Parameter> parameters, ReturnType returnType) {
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
        }

        public List<RustType> getDependentTypes() {
            List<RustType> dependentTypes = new LinkedList<>();
            if (null != this.returnType) {
                dependentTypes.add(this.returnType.rustType);
            }
            if (null != this.parameters) {
                this.parameters.stream().forEach((p) -> dependentTypes.add(p.rustType));
            }
            return dependentTypes;
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
