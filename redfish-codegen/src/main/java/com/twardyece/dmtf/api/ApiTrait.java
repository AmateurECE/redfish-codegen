package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

public class ApiTrait {
    public ApiTrait(CratePath path, PascalCaseName name, PathItem pathItem) {
        this.path = path;
        this.name = name;
        this.pathItem = pathItem;
    }

    CratePath path;
    PascalCaseName name;
    PathItem pathItem;
}
