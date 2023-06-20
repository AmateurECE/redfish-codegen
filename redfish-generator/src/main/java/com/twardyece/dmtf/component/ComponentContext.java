package com.twardyece.dmtf.component;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;

public class ComponentContext implements Comparable<ComponentContext> {
    public ModuleContext moduleContext;
    public RustType rustType;
    public List<Operation> operations;
    public List<Subcomponent> subcomponents;
    public List<RustType> owningComponents;

    public ComponentContext(RustType rustType) {
        this.moduleContext = new ModuleContext(rustType.getPath(), null);
        this.rustType = rustType;
        this.operations = new ArrayList<>();
        this.subcomponents = new ArrayList<>();
        this.owningComponents = new ArrayList<>();
    }

    public String componentName() { return this.rustType.getName().toString().toLowerCase(); }

    public static class Operation {
        public PascalCaseName pascalCaseName;
        public RustType requiredPrivilege;

        public Operation(PascalCaseName pascalCaseName, RustType requiredPrivilege) {
            this.pascalCaseName = pascalCaseName;
            this.requiredPrivilege = requiredPrivilege;
        }

        public SnakeCaseName snakeCaseName() { return new SnakeCaseName(this.pascalCaseName); }
    }
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
