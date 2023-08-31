package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.ICaseConvertible;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

public class ComponentTypeTranslationService {

    private final ModelResolver modelResolver;

    public ComponentTypeTranslationService(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    public RustType getRustTypeForComponentRef(String componentName) {
        RustType model =  this.modelResolver.resolvePath(componentName);
        return getRustType(model.getName());
    }

    public RustType getRustTypeForComponentName(String name) {
        return getRustType(new PascalCaseName(name));
    }

    private static RustType getRustType(ICaseConvertible name) {
        return new RustType(CratePath.parse("crate::" + new SnakeCaseName(name)),
                new PascalCaseName(name));
    }
}
