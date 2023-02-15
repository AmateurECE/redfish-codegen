package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelFile {
    private CratePath module;
    private Mustache template;
    private ModelContext context;

    public ModelFile(CratePath module, ModelContext context, Mustache template) {
        this.module = module;
        this.template = template;
        this.context = context;
    }

    public void registerModel(Map<String, ModuleFile> modules, FileFactory factory) {
        CratePath path = CratePath.empty();
        for (SnakeCaseName component : this.module.getComponents()) {
            if (!path.isEmpty()) {
                if (!modules.containsKey(path.toString())) {
                    modules.put(path.toString(), factory.makeModuleFile(path));
                }

                modules.get(path.toString()).addNamedSubmodule(component);
            }

            path = path.append(component);
        }

        // Can't forget to add this model as a module!
        if (!modules.containsKey(path.toString())) {
            modules.put(path.toString(), factory.makeModuleFile(path));
        }

        modules.get(path.toString()).addAnonymousSubmodule(this.context.getModule());
    }

    public void generate() throws IOException {
        File modelFile = this.module.append(context.modelModule).toPath().toFile();
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
