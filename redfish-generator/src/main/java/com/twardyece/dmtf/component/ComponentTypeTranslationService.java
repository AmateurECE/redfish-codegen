package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.ModelResolver;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

public class ComponentTypeTranslationService {

    private final ModelResolver modelResolver;

    public ComponentTypeTranslationService(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    public RustType getRustTypeForComponentName(String componentName) {
        RustType model =  this.modelResolver.resolvePath(componentName);
        return new RustType(CratePath.parse("crate::" + new SnakeCaseName(model.getName())),
                new PascalCaseName(model.getName()));
    }
}
