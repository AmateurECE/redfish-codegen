package com.twardyece.dmtf.specification;

import com.twardyece.dmtf.specification.file.IFileList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to discover versioned schema files in a directory.
 */
public class VersionedFileDiscovery {
    private final IFileList fileList;

    /**
     * Construct a VersionedFileDiscovery instance
     * @param fileList The files to filter.
     */
    public VersionedFileDiscovery(IFileList fileList) {
        this.fileList = fileList;
    }

    /**
     *
     * @param pattern The pattern to apply to filenames. Only files matching this pattern are considered. The pattern
     *                must use group names to allow VersionedFileDiscovery to query information about the file. These
     *                patterns will have two or more named capture groups: name, and version
     * @param nameGroupName The name of the capture group that can be used to obtain the schema name.
     * @param versionGroupName The name of the capture group that can be used to obtain the version of this file.
     * @param versionParsePattern A pattern that can be used to parse a semantically versioned identifier containing
     *                            three version parts: a major version, minor version and patch version.
     * @return A list of the highest version of all uniquely versioned files in the directory.
     */
    public List<VersionedFile> getFiles(Pattern pattern, String nameGroupName, String versionGroupName, Pattern versionParsePattern) {
        List<VersionedFile> allFiles = new ArrayList<>();
        for (String file : this.fileList.getFileList()) {
            Matcher matcher = pattern.matcher(file);
            if (!matcher.find()) {
                continue;
            }

            allFiles.add(new VersionedFile(
                    matcher.group(nameGroupName),
                    Version.parse(matcher.group(versionGroupName), versionParsePattern),
                    Paths.get(this.fileList.getRoot(), file)
            ));
        }

        List<VersionedFile> files = new ArrayList<>();
        List<String> uniqueNames = allFiles.stream().map((r) -> r.name).sorted().distinct().toList();
        for (String name : uniqueNames) {
            files.add(
                    allFiles
                            .stream()
                            .filter((file) -> file.name.equals(name))
                            .max(Comparator.comparing(one -> one.version))
                            .get()
            );
        }

        return files;
    }

    /**
     *
     * @param name The schema name. This is primarily just passed into the created VersionedFile object
     * @param pattern The pattern to apply to files in the directory. Only files that match are considered. This pattern
     *                must use named capture groups to allow this class to query information of the file.
     * @param versionGroupName The name of the capture group that can be used to obtain the version of files that match
     *                         the provided pattern.
     * @param versionParsePattern A pattern that can be used to split the version string into major version, minor
     *                            version, and patch version.
     * @return The path of the file that represents the most recent version of the versioned schema files matching the
     * pattern.
     */
    public Optional<VersionedFile> getFile(String name, Pattern pattern, String versionGroupName, Pattern versionParsePattern) {
        List<VersionedFile> versionedFileVersions = new ArrayList<>();
        for (String file : this.fileList.getFileList()) {
            Matcher matcher = pattern.matcher(file);
            if (!matcher.find()) {
                continue;
            }

            versionedFileVersions.add(new VersionedFile(
                    name,
                    Version.parse(matcher.group(versionGroupName), versionParsePattern),
                    Paths.get(this.fileList.getRoot(), file)
            ));
        }

        return versionedFileVersions.stream()
                .max(Comparator.comparing(one -> one.version));
    }

    public static class VersionedFile {
        public String name;
        public Version version;
        public Path file;

        public VersionedFile(String name, Version version, Path file) {
            this.name = name;
            this.version = version;
            this.file = file;
        }
    }
}
