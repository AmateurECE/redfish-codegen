package com.twardyece.dmtf.component;

import com.twardyece.dmtf.ModuleContext;

import java.net.URI;
import java.util.List;

public record MetadataRoutingContext(ModuleContext module, String serviceRootVersion, List<Reference> references) {
    public record Reference(URI uri, List<Namespace> namespaces) {
        public record Namespace(String name) {}
    }
}
