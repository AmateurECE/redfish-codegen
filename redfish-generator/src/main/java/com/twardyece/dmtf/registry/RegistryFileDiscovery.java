package com.twardyece.dmtf.registry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistryFileDiscovery {
    private Path registryDirectory;
    private static final Pattern pattern = Pattern.compile("^(?<name>[A-Za-z0-9]+)[._](?<version>[0-9.]+)\\.json$");

    public RegistryFileDiscovery(Path registryDirectory) {
        this.registryDirectory = registryDirectory;
    }

    public List<Registry> getRegistries() {
        List<Registry> allRegistries = new ArrayList<>();
        for (String file : Objects.requireNonNull(this.registryDirectory.toFile().list())) {
            Matcher matcher = pattern.matcher(file);
            if (!matcher.find()) {
                continue;
            }

            allRegistries.add(new Registry(matcher.group("name"), matcher.group("version"),
                    Paths.get(this.registryDirectory.toString(), file)));
        }

        List<Registry> registries = new ArrayList<>();
        List<String> uniqueNames = allRegistries.stream().map((r) -> r.name).sorted().distinct().toList();
        for (String name : uniqueNames) {
            List<String> versions = allRegistries.stream()
                    .filter((r) -> r.name.equals(name))
                    .map((r) -> r.version)
                    .toList();
            String highestVersion = getHighestVersion(versions);
            registries.add(allRegistries.stream()
                    .filter((r) -> r.name.equals(name) && r.version.equals(highestVersion))
                    .findFirst()
                    .get());
        }

        return registries;
    }

    private static String getHighestVersion(List<String> versionStrings) {
        Version highestVersion = versionStrings.stream()
                .map(Version::parse)
                .max(Version::compareTo)
                .get();

        return highestVersion.toString();
    }

    public static class Registry {
        public String name;
        public String version;
        public Path file;

        public Registry(String name, String version, Path file) {
            this.name = name;
            this.version = version;
            this.file = file;
        }
    }
}
