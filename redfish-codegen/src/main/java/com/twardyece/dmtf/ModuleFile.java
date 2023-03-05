package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

public class ModuleFile {
    private ModuleContext context;
    private Mustache template;

    public ModuleFile(ModuleContext context, Mustache template) {
        this.context = context;
        this.template = template;
    }

    public void generate() throws IOException {
        File moduleFile = this.context.path.toPath().toFile();
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
