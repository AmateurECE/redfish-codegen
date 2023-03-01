package com.twardyece.dmtf;

import com.github.mustachejava.Mustache;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

public class ModuleFile {
    private CratePath path;
    private HashMap<SnakeCaseName, ModuleContext.Submodule> submodules;
    private Mustache template;

    public ModuleFile(CratePath path, Mustache template) {
        this.path = path;
        this.submodules = new HashMap<>();
        this.template = template;
    }

    // Namespace elements from "named" submodules are not re-exported in the parent submodule, so their names must
    // prefix names of any namespace elements they export.
    public void addNamedSubmodule(SnakeCaseName name) {
        // TODO: Instead of calling escapeReservedKeyword here, create a SanitarySnakeCaseIdentifier class that
        // ensures the identifier can be used in Rust code.
        this.submodules.put(name, new ModuleContext.Submodule(RustConfig.escapeReservedKeyword(name), false));
    }

    // All exported namespace elements from anonymous submodules are re-exported from the parent namespace, like so:
    //   mod name;
    //   pub use name::*;
    // This makes them essentially "invisible" when referring to structs by path.
    public void addAnonymousSubmodule(SnakeCaseName name) {
        this.submodules.put(name, new ModuleContext.Submodule(RustConfig.escapeReservedKeyword(name), true));
    }

    public void generate() throws IOException {
        File moduleFile = this.path.toPath().toFile();
        File parent = moduleFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        moduleFile.createNewFile();

        // Construct context
        ModuleContext context = new ModuleContext(this.submodules.values());

        // Render the template
        Writer writer = new PrintWriter(moduleFile);
        this.template.execute(writer, context);
        writer.flush();
    }
}
