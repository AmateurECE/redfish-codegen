package com.twardyece.dmtf.model;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.FileFactory;
import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.context.ModelContext;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ModelFile {
    private CratePath module;
    private Mustache template;
    private ModelContext context;

    public ModelFile(ModelContext context, Mustache template) {
        this.module = context.rustType.getPath().append(new SnakeCaseName(context.rustType.getName()));
        this.template = template;
        this.context = context;
    }

    public void registerModel(Map<String, ModuleFile> modules, FileFactory factory) {
        if (null == this.module.getComponents() || 2 > this.module.getComponents().size()) {
            return;
        }

        List<SnakeCaseName> startingPath = new ArrayList<>();
        startingPath.add(this.module.getComponents().get(1));
        CratePath path = CratePath.crateLocal(startingPath);

        for (int i = 2; i < this.module.getComponents().size(); ++i) {
            SnakeCaseName component = this.module.getComponents().get(i);
            if (!path.isEmpty()) {
                if (!modules.containsKey(path.toString())) {
                    modules.put(path.toString(), factory.makeModuleFile(path));
                }

                if (this.module.getComponents().size() - 1 == i) {
                    modules.get(path.toString()).addAnonymousSubmodule(component);
                } else {
                    modules.get(path.toString()).addNamedSubmodule(component);
                }
            }

            path = path.append(component);
        }
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
