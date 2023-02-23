package com.twardyece.dmtf.api;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.CratePath;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class TraitFile {
    private final CratePath module;
    private final TraitContext context;
    private final Mustache template;

    public TraitFile(CratePath module, TraitContext context, Mustache template) {
        this.module = module;
        this.context = context;
        this.template = template;
    }

    public void generate() throws IOException {
        File modelFile = this.module.toPath().toFile();
        File parent = modelFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        modelFile.createNewFile();

        // Render the template
        Writer writer = new PrintWriter(modelFile);
        this.template.execute(writer, this.context);
        writer.flush();
    }
}
