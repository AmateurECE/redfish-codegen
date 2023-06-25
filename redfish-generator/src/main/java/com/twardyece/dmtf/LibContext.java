package com.twardyece.dmtf;

public class LibContext {
    public ModuleContext moduleContext;
    public String specVersion;
    public String additionalContent;

    public LibContext(ModuleContext moduleContext, String specVersion) {
        this.moduleContext = moduleContext;
        this.specVersion = specVersion;
    }

    public LibContext(ModuleContext moduleContext, String specVersion, String additionalContent) {
        this.moduleContext = moduleContext;
        this.specVersion = specVersion;
        this.additionalContent = additionalContent;
    }
}
