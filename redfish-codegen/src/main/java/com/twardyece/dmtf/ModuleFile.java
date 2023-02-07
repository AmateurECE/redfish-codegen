package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ModuleFile {
    private String path;
    private ArrayList<SnakeCaseName> submodules;
    private Mustache template;

    public ModuleFile(String path, Mustache template) {
        this.path = path;
        this.submodules = new ArrayList<>();
        this.template = template;
    }

    public void addSubmodule(SnakeCaseName name) {
        this.submodules.add(name);
    }

    public void generate() throws IOException {
        // TODO: Render the template!
        File versionFile = new File(this.path);
        versionFile.createNewFile();
    }
}
