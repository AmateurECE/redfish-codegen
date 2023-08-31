package com.twardyece.dmtf.component;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.text.PascalCaseName;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

    public List<Pair<String, OperationPrivilegeMapping>> getSubordinatePrivilegeOverridesForComponent(PascalCaseName componentName) {
        List<Pair<String, OperationPrivilegeMapping>> overrides = new ArrayList<>();
        String name = componentName.toString();
        for (int i = 0; i < this.mappings.length(); ++i) {
            JSONObject mapping = this.mappings.getJSONObject(i);
            if (!mapping.has("SubordinateOverrides")) {
                continue;
            }
            JSONObject defaultMapping = mapping.getJSONObject("OperationMap");
            JSONArray subordinateOverrides = mapping.getJSONArray("SubordinateOverrides");
            for (int j = 0; j < subordinateOverrides.length(); ++j) {
                JSONObject subordinateOverride = subordinateOverrides.getJSONObject(j);
                JSONArray targets = subordinateOverride.getJSONArray("Targets");
                for (int k = 0; k < targets.length(); ++k) {
                    if (targets.getString(k).equals(name)) {
                        JSONObject overriddenOperationMap = subordinateOverride.getJSONObject("OperationMap");
                        OperationPrivilegeMapping operationPrivilegeMapping = new OperationPrivilegeMapping(
                                parseOperationMap(getMappingOrOverride(defaultMapping, overriddenOperationMap, "GET")),
                                parseOperationMap(getMappingOrOverride(defaultMapping, overriddenOperationMap, "HEAD")),
                                parseOperationMap(getMappingOrOverride(defaultMapping, overriddenOperationMap, "POST")),
                                parseOperationMap(getMappingOrOverride(defaultMapping, overriddenOperationMap, "PUT")),
                                parseOperationMap(getMappingOrOverride(defaultMapping, overriddenOperationMap, "PATCH")),
                                parseOperationMap(getMappingOrOverride(defaultMapping, overriddenOperationMap, "DELETE"))
                        );

                        overrides.add(new ImmutablePair<>(mapping.getString("Entity"), operationPrivilegeMapping));
                    }
                }
            }
        }

        return overrides;
    }

    private JSONArray getMappingOrOverride(JSONObject mapping, JSONObject overrides, String key) {
        if (overrides.has(key)) {
            return overrides.getJSONArray(key);
        } else {
            return mapping.getJSONArray(key);
        }
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
    public record SubordinatePrivilegeOverride(PascalCaseName owningComponent,
                                               PrivilegeRegistry.OperationPrivilegeMapping privileges) {}
}
