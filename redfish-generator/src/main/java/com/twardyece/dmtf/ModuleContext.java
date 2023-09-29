package com.twardyece.dmtf;

import com.twardyece.dmtf.rust.RustConfig;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleContext {
    public CratePath path;
    private final Set<Submodule> submoduleSet;
    public Set<Import> imports;

    public ModuleContext(CratePath path) {
        this.path = path;
        this.submoduleSet = new HashSet<>();
    }

    public List<Submodule> submodules() { return this.submoduleSet.stream().sorted().collect(Collectors.toList()); }

    // Namespace elements from "named" submodules are not re-exported in the parent submodule, so their names must
    // prefix names of any namespace elements they export.
    public void addNamedSubmodule(SnakeCaseName name) {
        this.addNamedSubmoduleWithFeature(name, null);
    }

    // All exported namespace elements from anonymous submodules are re-exported from the parent namespace, like so:
    //   mod name;
    //   pub use name::*;
    // This makes them essentially "invisible" when referring to structs by path.
    public void addAnonymousSubmodule(SnakeCaseName name) {
        this.submoduleSet.add(new ModuleContext.Submodule(RustConfig.escapeReservedKeyword(name), true, null));
    }

    public void addNamedSubmoduleWithFeature(SnakeCaseName name, String feature) {
        // TODO: Instead of calling escapeReservedKeyword here, create a SanitarySnakeCaseIdentifier class that
        // ensures the identifier can be used in Rust code.
        this.submoduleSet.add(new ModuleContext.Submodule(RustConfig.escapeReservedKeyword(name), false, feature));
    }

    public void registerModule(Map<String, ModuleContext> modules) {
        List<SnakeCaseName> components = this.path.getComponents();
        if (null == components || 2 > components.size()) {
            return;
        }

        List<SnakeCaseName> startingPath = new ArrayList<>();
        startingPath.add(components.get(1));
        CratePath path = CratePath.crateLocal(startingPath);

        for (int i = 2; i < components.size(); ++i) {
            SnakeCaseName component = components.get(i);
            if (!path.isEmpty()) {
                if (!modules.containsKey(path.toString())) {
                    modules.put(path.toString(), new ModuleContext(path));
                }

                if (components.size() - 1 == i) {
                    modules.get(path.toString()).addAnonymousSubmodule(component);
                } else {
                    modules.get(path.toString()).addNamedSubmodule(component);
                }
            }

            path = path.append(component);
        }
    }

    public static class Import implements Comparable<Import> {
        public Import(CratePath cratePath) {
            this.cratePath = cratePath;
        }

        CratePath cratePath;

        public String path() { return this.cratePath.toString(); }

        @Override
        public int compareTo(Import i) {
            return this.cratePath.compareTo(i.cratePath);
        }

        @Override
        public int hashCode() { return this.cratePath.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Import) {
                return this.cratePath.equals(((Import) o).cratePath);
            } else {
                return false;
            }
        }

        @Override
        public String toString() { return this.cratePath.toString(); }
    }

    static class Submodule implements Comparable<Submodule> {
        Submodule(SnakeCaseName name, boolean isUsed, String feature) {
            this.snakeCaseName = name;
            this.isUsed = isUsed;
            this.feature = feature;
        }

        String name() { return this.snakeCaseName.toString(); }

        SnakeCaseName snakeCaseName;
        boolean isUsed;
        String feature;

        @Override
        public int compareTo(Submodule submodule) {
            if (!this.isUsed && submodule.isUsed) {
                return -1;
            } else if (this.isUsed && !submodule.isUsed) {
                return 1;
            } else {
                return this.snakeCaseName.compareTo(submodule.snakeCaseName);
            }
        }

        @Override
        public int hashCode() { return this.snakeCaseName.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Submodule) {
                return this.snakeCaseName.equals(((Submodule) o).snakeCaseName);
            } else {
                return false;
            }
        }

        @Override
        public String toString() { return this.snakeCaseName.toString(); }
    }
}
