package com.twardyece.dmtf.model;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.FileFactory;
import com.twardyece.dmtf.ModuleFile;
import com.twardyece.dmtf.model.ModelContext;
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

    public ModelFile(CratePath module, ModelContext context, Mustache template) {
        this.module = module;
        this.template = template;
        this.context = context;
    }

    public void registerModel(Map<String, ModuleFile> modules, FileFactory factory) {
        if (null == this.module.getComponents() || 2 > this.module.getComponents().size()) {
            return;
        }

        Iterator<SnakeCaseName> iterator = this.module.getComponents().listIterator(2);
        List<SnakeCaseName> startingPath = new ArrayList<>();
        startingPath.add(this.module.getComponents().get(1));

        CratePath path = CratePath.crateLocal(startingPath);
        while (iterator.hasNext()) {
            SnakeCaseName component = iterator.next();
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

        modules.get(path.toString()).addAnonymousSubmodule(this.context.modelModule);
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
