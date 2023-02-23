package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;

public class ApiTrait {
    public ApiTrait(CratePath path, PascalCaseName name, PathItem pathItem, List<String> mountpoints) {
        this.path = path;
        this.name = name;
        this.pathItem = pathItem;
        this.mountpoints = mountpoints;
    }

    CratePath path;
    PascalCaseName name;
    PathItem pathItem;
    List<String> mountpoints;
}
