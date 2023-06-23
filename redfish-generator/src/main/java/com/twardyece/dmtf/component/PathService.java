package com.twardyece.dmtf.component;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public class PathService {
    public String getClosestParent(Collection<String> paths, String path) {
        // Determine the closest parent path, which is the largest substring by component from the set of valid paths.

        // Remove a trailing slash, if there is one, because this creates an extra empty element in the component array.
        String firstPath = removeTrailingSlash(path);

        // Split the path into components separated by '/'
        String[] firstComponents = firstPath.split("/");

        // Start with a pessimistic parent.
        Pair<Integer, String> closestParent = new ImmutablePair<>(0, "");

        // For each path in the set...
        for (String secondPath : paths) {
            String[] secondComponents = secondPath.split("/");
            if (secondComponents.length >= firstComponents.length) {
                continue;
            }

            int i;
            boolean finishedEarly = false;

            // For each component in the second path...
            for (i = 0; i < secondComponents.length; ++i) {
                // If this component does not match the component at the same position in the first path, quit early.
                if (!firstComponents[i].equals(secondComponents[i])) {
                    finishedEarly = true;
                    break;
                }
            }

            // If we didn't break early, and the first path has one extra component than the second path, this is a
            // direct parent of the first path.
            if (!finishedEarly && firstComponents.length == secondComponents.length + 1) {
                return secondPath;
            }
            // If we did finish early, and this path matched a greater number of components than the previous closest
            // parent, make this the new closest parent.
            else if (!finishedEarly && i > closestParent.getLeft()) {
                closestParent = new ImmutablePair<>(i, secondPath);
            }
        }

        // If the closest parent is still the pessimistic guess, we failed.
        if (closestParent.getRight().equals("")) {
            throw new NoCloseParentException(firstPath);
        } else {
            return closestParent.getRight();
        }
    }

    public static String removeTrailingSlash(String path) {
        int endIndex = path.length();
        if ('/' == path.charAt(path.length() - 1)) {
            endIndex = path.length() - 1;
        }

        return path.substring(0, endIndex);
    }

    public static class NoCloseParentException extends RuntimeException {
        public NoCloseParentException(String path) {
            super("There is no close relative of path " + path);
        }
    }
}
