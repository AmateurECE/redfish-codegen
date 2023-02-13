package com.twardyece.dmtf;

import com.fasterxml.jackson.databind.Module;
import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
        if (!this.submodules.contains(name)) {
            this.submodules.add(name);
        }
    }

    public void generate() throws IOException {
        File moduleFile = new File(this.path);
        File parent = moduleFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        moduleFile.createNewFile();

        // Construct context
        ModuleContext context = new ModuleContext(this.submodules.stream().sorted().distinct().collect(Collectors.toList()));

        // Render the template
        Writer writer = new PrintWriter(moduleFile);
        this.template.execute(writer, context);
        writer.flush();
    }
}
