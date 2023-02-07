package com.twardyece.dmtf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ModuleFile {
    private String path;
    private ArrayList<SnakeCaseName> submodules;

    public ModuleFile(String path) {
        this.path = path;
        this.submodules = new ArrayList<>();
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
