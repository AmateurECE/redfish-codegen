package com.twardyece.dmtf.specification.file;

import java.util.List;

/**
 * Generic interface representing types that can obtain a list of files
 */
public interface IFileList {
    List<String> getFileList();
    String getRoot();
}
