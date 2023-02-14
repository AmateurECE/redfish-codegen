package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CratePath {
    private ArrayList<SnakeCaseName> path;

    public static CratePath crateLocal(Collection<SnakeCaseName> path) {
        CratePath cratePath = new CratePath();
        cratePath.path = new ArrayList<>();
        cratePath.path.add(RustConfig.CRATE_ROOT_MODULE);
        cratePath.path.addAll(path);
        return cratePath;
    }

    public static CratePath relative(Collection<SnakeCaseName> path) {
        CratePath cratePath = new CratePath();
        cratePath.path = new ArrayList<>();
        cratePath.path.addAll(path);
        return cratePath;
    }

    public List<SnakeCaseName> getComponents() { return this.path; }

    public boolean isCrateLocal() {
        return null != this.path.get(0) && this.path.get(0).equals(RustConfig.CRATE_ROOT_MODULE);
    }

    @Override
    public String toString() {
        return String.join("::", this.path.stream().map((name) -> name.toString()).collect(Collectors.toList()));
    }

    public String joinType(RustType type) {
        return this + "::" + type.toString();
    }

}
