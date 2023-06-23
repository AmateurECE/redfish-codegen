package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.registry.RegistryContext;
import com.twardyece.dmtf.text.PascalCaseName;
import io.swagger.v3.oas.models.PathItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PrivilegeRegistry {
    private final CratePath privilegePath;
    private final JSONArray mappings;

    public PrivilegeRegistry(Path privilegeRegistry, CratePath privilegePath) throws IOException {
        JSONObject object = new JSONObject(Files.readString(privilegeRegistry));
        this.mappings = object.getJSONArray("Mappings");
        this.privilegePath = privilegePath;
    }

    public OperationPrivilegeMapping getPrivilegesForComponent(PascalCaseName componentName) {
        String name = componentName.toString();
        for (int i = 0; i < this.mappings.length(); ++i) {
            JSONObject mapping = this.mappings.getJSONObject(i);
            if (!mapping.getString("Entity").equals(name)) {
                continue;
            }

            JSONObject operationMap = mapping.getJSONObject("OperationMap");
            return new OperationPrivilegeMapping(
                    parseOperationMap(operationMap.getJSONArray("GET")),
                    parseOperationMap(operationMap.getJSONArray("HEAD")),
                    parseOperationMap(operationMap.getJSONArray("POST")),
                    parseOperationMap(operationMap.getJSONArray("PUT")),
                    parseOperationMap(operationMap.getJSONArray("PATCH")),
                    parseOperationMap(operationMap.getJSONArray("DELETE"))
            );
        }
        return null;
    }

    private RustType parseOperationMap(JSONArray conjunctivePrivileges) {
        List<List<RustType>> privileges = new ArrayList<>();
        for (int i = 0; i < conjunctivePrivileges.length(); ++i) {
            JSONArray disjunctivePrivileges = conjunctivePrivileges.getJSONObject(i)
                    .getJSONArray("Privilege");
            List<RustType> disjunction = new ArrayList<>();
            for (int j = 0; j < disjunctivePrivileges.length(); ++j) {
                disjunction.add(new RustType(this.privilegePath, new PascalCaseName(disjunctivePrivileges.getString(j))));
            }
            privileges.add(disjunction);
        }

        return privileges
                .stream()
                .map((set) -> set
                        .stream()
                        .reduce((one, two) -> new RustType(this.privilegePath, new PascalCaseName("And"), new RustType[]{one, two}))
                        .get()
                )
                .reduce((one, two) -> new RustType(this.privilegePath, new PascalCaseName("Or"), new RustType[]{one, two}))
                .get();
    }

    public record OperationPrivilegeMapping(RustType get, RustType head, RustType post, RustType put, RustType patch,
                                            RustType delete) {}
}
