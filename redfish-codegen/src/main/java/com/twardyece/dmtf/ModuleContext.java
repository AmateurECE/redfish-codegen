package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleContext {
    CratePath path;
    Set<Submodule> submoduleSet;

    public ModuleContext(CratePath path) {
        this.path = path;
        this.submoduleSet = new HashSet<>();
    }

    public List<Submodule> submodules() { return this.submoduleSet.stream().sorted().collect(Collectors.toList()); }

    // Namespace elements from "named" submodules are not re-exported in the parent submodule, so their names must
    // prefix names of any namespace elements they export.
    public void addNamedSubmodule(SnakeCaseName name) {
        // TODO: Instead of calling escapeReservedKeyword here, create a SanitarySnakeCaseIdentifier class that
        // ensures the identifier can be used in Rust code.
        this.submoduleSet.add(new ModuleContext.Submodule(RustConfig.escapeReservedKeyword(name), false));
    }

    // All exported namespace elements from anonymous submodules are re-exported from the parent namespace, like so:
    //   mod name;
    //   pub use name::*;
    // This makes them essentially "invisible" when referring to structs by path.
    public void addAnonymousSubmodule(SnakeCaseName name) {
        this.submoduleSet.add(new ModuleContext.Submodule(RustConfig.escapeReservedKeyword(name), true));
    }

    static class Submodule implements Comparable<Submodule> {
        Submodule(SnakeCaseName name, boolean isUsed) {
            this.snakeCaseName = name;
            this.isUsed = isUsed;
        }

        String name() { return this.snakeCaseName.toString(); }

        SnakeCaseName snakeCaseName;
        boolean isUsed;

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
