package com.twardyece.dmtf.component;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.*;

public class ComponentContext implements Comparable<ComponentContext> {
    public ModuleContext moduleContext;
    public RustType rustType;
    public Map<PathItem.HttpMethod, Operation> operationMap;
    public List<Subcomponent> subcomponents;
    public List<RustType> owningComponents;

    public ComponentContext(RustType rustType) {
        this.moduleContext = new ModuleContext(rustType.getPath(), null);
        this.rustType = rustType;
        this.operationMap = new HashMap<>();
        this.subcomponents = new ArrayList<>();
        this.owningComponents = new ArrayList<>();
    }

    public PascalCaseName componentName() {
        return new PascalCaseName(this.rustType.getName());
    }
    public Collection<Operation> operations() { return this.operationMap.values(); }

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
