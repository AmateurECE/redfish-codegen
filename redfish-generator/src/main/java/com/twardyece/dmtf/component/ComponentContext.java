package com.twardyece.dmtf.component;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.LinkedList;
import java.util.List;

public class ComponentContext implements Comparable<ComponentContext> {
    public final ModuleContext moduleContext;
    public final RustType rustType;
    public final List<Operation> operations;
    public final List<Subcomponent> subcomponents;
    public final List<RustType> owningComponents;

    public ComponentContext(RustType rustType, List<Operation> operations, List<Subcomponent> subcomponents, List<RustType> owningComponents) {
        this.moduleContext = new ModuleContext(rustType.getPath(), null);
        this.rustType = rustType;
        this.operations = operations;
        this.subcomponents = subcomponents;
        this.owningComponents = owningComponents;
    }

    public String componentName() { return this.rustType.getName().toString().toLowerCase(); }

    public record Operation(String operationName, RustType requiredPrivilege) {}
    public record Subcomponent(String componentName, RustType component) {}

    @Override
    public String toString() { return this.rustType.toString(); }

    @Override
    public int compareTo(ComponentContext o) { return this.rustType.compareTo(o.rustType); }

    @Override
    public int hashCode() {
        return this.rustType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ComponentContext) {
            return this.rustType.equals(((ComponentContext) o).rustType);
        } else {
            return false;
        }
    }
}
