package com.twardyece.dmtf.component;

import java.util.Collection;
import java.util.List;

public class PathService {
    public String getMountpoint(Collection<String> paths, String path) {
        // Determine the parent path, which is the largest substring from the set of valid paths
        String firstPath = removeTrailingSlash(path);
        String maxSubstring = "";
        for (String secondPath : paths) {
            if (firstPath.contains(secondPath) && secondPath.length() > maxSubstring.length() && !firstPath.equals(secondPath)) {
                maxSubstring = secondPath;
            }
        }

        return maxSubstring;
    }

    public static String removeTrailingSlash(String path) {
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        return path.substring(0, endIndex);
    }
}
