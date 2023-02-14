package com.twardyece.dmtf;

import com.twardyece.dmtf.text.SnakeCaseName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CratePath {
    private ArrayList<SnakeCaseName> path;

    public static CratePath crateLocal(Collection<SnakeCaseName> path) {
        CratePath cratePath = CratePath.crateRoot();
        cratePath.path.addAll(path);
        return cratePath;
    }

    public static CratePath crateRoot() {
        CratePath cratePath = CratePath.empty();
        cratePath.path.add(RustConfig.CRATE_ROOT_MODULE);
        return cratePath;
    }

    public static CratePath relative(Collection<SnakeCaseName> path) {
        CratePath cratePath = CratePath.empty();
        cratePath.path.addAll(path);
        return cratePath;
    }

    public static CratePath empty() {
        CratePath cratePath = new CratePath();
        cratePath.path = new ArrayList<>();
        return cratePath;
    }

    public List<SnakeCaseName> getComponents() { return this.path; }

    public boolean isCrateLocal() {
        return null != this.path.get(0) && this.path.get(0).equals(RustConfig.CRATE_ROOT_MODULE);
    }

    public boolean isEmpty() { return 0 == this.path.size(); }

    @Override
    public String toString() {
        return String.join("::", this.path.stream().map((name) -> name.toString()).collect(Collectors.toList()));
    }

    public String joinType(RustType type) {
        return this + "::" + type.toString();
    }

    public CratePath append(SnakeCaseName component) {
        CratePath cratePath = CratePath.empty();
        cratePath.path.addAll(this.path);
        cratePath.path.add(component);
        return cratePath;
    }

    public Path toPath() {
        if (path.isEmpty()) {
            return null;
        } else if (1 == path.size() && path.get(0).equals(RustConfig.CRATE_ROOT_MODULE)) {
            return Paths.get(RustConfig.CRATE_SOURCE_DIRECTORY + "/" + RustConfig.CRATE_ROOT_FILE);
        } else if (path.get(0).equals(RustConfig.CRATE_ROOT_MODULE)) {
            path.set(0, RustConfig.CRATE_SOURCE_DIRECTORY);
        }

        String path = String.join("/", this.path.stream().map((name) -> name.toString()).collect(Collectors.toList()))
                + RustConfig.FILE_EXTENSION;
        return Paths.get(path);
    }
}
