package com.twardyece.dmtf.specification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    public int major;
    public int minor;
    public int patch;
    private static final Pattern defaultPattern = Pattern.compile("([0-9]+).([0-9]+).([0-9]+)");

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static Version parse(String value, Pattern pattern) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            throw new RuntimeException("Invalid version string " + value);
        }

        return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
    }

    public static Version parse(String value) {
        return parse(value, defaultPattern);
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch;
    }

    @Override
    public int compareTo(Version o) {
        if (this.major < o.major) {
            return -1;
        } else if (this.major > o.major) {
            return 1;
        } else {
            if (this.minor < o.minor) {
                return -1;
            } else if (this.minor > o.minor) {
                return 1;
            } else {
                if (this.patch < o.patch) {
                    return -1;
                } else if (this.patch > o.patch) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
