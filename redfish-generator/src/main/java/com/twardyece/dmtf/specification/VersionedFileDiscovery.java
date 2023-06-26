package com.twardyece.dmtf.specification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedFileDiscovery {
    private final Path directory;

    public VersionedFileDiscovery(Path directory) {
        this.directory = directory;
    }

    public List<VersionedFile> getFiles(Pattern pattern, String nameGroupName, String versionGroupName, Pattern versionParsePattern) {
        List<VersionedFile> allFiles = new ArrayList<>();
        for (String file : Objects.requireNonNull(this.directory.toFile().list())) {
            Matcher matcher = pattern.matcher(file);
            if (!matcher.find()) {
                continue;
            }

            allFiles.add(new VersionedFile(
                    matcher.group(nameGroupName),
                    Version.parse(matcher.group(versionGroupName), versionParsePattern),
                    Paths.get(this.directory.toString(), file)
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

    public Optional<VersionedFile> getFile(String name, Pattern pattern, String versionGroupName, Pattern versionParsePattern) {
        List<VersionedFile> versionedFileVersions = new ArrayList<>();
        for (String file : Objects.requireNonNull(this.directory.toFile().list())) {
            Matcher matcher = pattern.matcher(file);
            if (!matcher.find()) {
                continue;
            }

            versionedFileVersions.add(new VersionedFile(
                    name,
                    Version.parse(matcher.group(versionGroupName), versionParsePattern),
                    Paths.get(this.directory.toString(), file)
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
