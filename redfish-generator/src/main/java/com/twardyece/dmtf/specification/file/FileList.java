package com.twardyece.dmtf.specification.file;

import java.nio.file.Path;
import java.util.List;

public class FileList implements IFileList {
    private final List<String> fileList;
    private final String rootDirectory;

    public FileList(List<String> fileList, String rootDirectory) {
        this.fileList = fileList;
        this.rootDirectory = rootDirectory;
    }

    @Override
    public List<String> getFileList() {
        return this.fileList;
    }

    @Override
    public String getRoot() {
        return this.rootDirectory;
    }
}
