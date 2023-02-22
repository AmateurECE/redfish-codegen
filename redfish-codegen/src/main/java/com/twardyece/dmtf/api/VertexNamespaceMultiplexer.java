package com.twardyece.dmtf.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VertexNamespaceMultiplexer {
    private Pattern pattern;
    private String groupName;
    private String vertexName;

    public VertexNamespaceMultiplexer(String vertexName, Pattern pattern, String groupName) {
        this.vertexName = vertexName;
        this.pattern = pattern;
        this.groupName = groupName;
    }

    public String mux(String name, String path) {
        if (name.equals(this.vertexName)) {
            Matcher matcher = this.pattern.matcher(path);
            if (matcher.find()) {
                return matcher.group(this.groupName) + "::" + name;
            } else {
                throw new RuntimeException("Name matches, but regex does not.");
            }
        } else {
            return name;
        }
    }

    public String demux(String name) {
        String[] components = name.split("::");
        return components[components.length - 1];
    }
}
