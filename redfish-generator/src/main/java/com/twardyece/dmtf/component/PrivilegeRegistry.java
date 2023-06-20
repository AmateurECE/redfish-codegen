package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.nio.file.Path;

public class PrivilegeRegistry {

    private final CratePath privilegePath;

    public PrivilegeRegistry(Path privilegeRegistry, CratePath privilegePath) {
        this.privilegePath = privilegePath;
    }

    public RustType getPrivilegeForComponent(PascalCaseName componentName, PathItem.HttpMethod method) {
        // TODO: Fill in this implementation
        return new RustType(this.privilegePath, new PascalCaseName("Login"));
    }
}
