package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import io.swagger.v3.oas.models.PathItem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PrivilegeRegistry {
    public PrivilegeRegistry(Path privilegeRegistry, CratePath privilegePath) {}

    public Map<PathItem.HttpMethod, RustType> getPrivilegesRequiredForComponent(String componentName) {
        Map<PathItem.HttpMethod, RustType> privilegeMap = new HashMap<>();
        return privilegeMap;
    }
}
