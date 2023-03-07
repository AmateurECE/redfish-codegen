package com.twardyece.dmtf;

public class LibContext {
    public ModuleContext moduleContext;
    public String specVersion;

    public LibContext(ModuleContext moduleContext, String specVersion) {
        this.moduleContext = moduleContext;
        this.specVersion = specVersion;
    }
}
