package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ModuleFile<T> {
    CratePath path;
    private T context;
    private Mustache template;

    public ModuleFile(CratePath path, T context, Mustache template) {
        this.path = path;
        this.context = context;
        this.template = template;
    }

    public T getContext() { return this.context; }

    public void generate() throws IOException {
        File moduleFile = this.path.toPath().toFile();
        File parent = moduleFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        moduleFile.createNewFile();

        // Render the template
        Writer writer = new PrintWriter(moduleFile);
        this.template.execute(writer, this.context);
        writer.flush();
    }
}
