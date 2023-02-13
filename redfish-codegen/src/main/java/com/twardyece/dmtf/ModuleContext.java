package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleContext {
    List<Submodule> submodules;

    public ModuleContext(List<SnakeCaseName> submodules) {
        this.submodules = submodules.stream()
                .map((SnakeCaseName name) -> new Submodule(name.toString()))
                .collect(Collectors.toList());
    }

    static class Submodule {
        Submodule(String name) {
            this.name = name;
        }

        String name;
    }
}
