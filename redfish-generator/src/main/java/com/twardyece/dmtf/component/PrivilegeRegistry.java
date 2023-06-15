package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PrivilegeRegistry {

    private final CratePath privilegePath;

    public PrivilegeRegistry(Path privilegeRegistry, CratePath privilegePath) {
        this.privilegePath = privilegePath;
    }

    public Map<PathItem.HttpMethod, RustType> getPrivilegesRequiredForComponent(String componentName) {
        // TODO: Fill in this implementation
        RustType login = new RustType(this.privilegePath, new PascalCaseName("Login"));
        Map<PathItem.HttpMethod, RustType> privilegeMap = new HashMap<>();
        privilegeMap.put(PathItem.HttpMethod.GET, login);
        privilegeMap.put(PathItem.HttpMethod.POST, login);
        privilegeMap.put(PathItem.HttpMethod.PUT, login);
        privilegeMap.put(PathItem.HttpMethod.PATCH, login);
        return privilegeMap;
    }
}
