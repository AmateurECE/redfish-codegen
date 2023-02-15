package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleContext {
    List<Submodule> submodules;

    public ModuleContext(Collection<Submodule> submodules) {
        this.submodules = submodules.stream().sorted().distinct().collect(Collectors.toList());
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
    }
}
