package com.twardyece.dmtf.specification.file;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DirectoryFileList implements IFileList {
    private final Path directory;

    public DirectoryFileList(Path directory) {
        this.directory = directory;
    }

    @Override
    public List<String> getFileList() {
        return Arrays.stream(Objects.requireNonNull(this.directory.toFile().list())).toList();
    }

    @Override
    public String getRoot() {
        return this.directory.toString();
    }
}
