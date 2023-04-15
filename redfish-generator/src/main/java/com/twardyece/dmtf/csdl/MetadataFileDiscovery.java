package com.twardyece.dmtf.csdl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MetadataFileDiscovery {
    private final String csdlDirectory;

    public MetadataFileDiscovery(String csdlDirectory) {
        this.csdlDirectory = csdlDirectory;
    }

    public List<MetadataRoutingContext.Reference> getReferences() throws URISyntaxException {
        List<MetadataRoutingContext.Reference> references = new ArrayList<>();
        List<MetadataRoutingContext.Reference.Namespace> namespaces = new ArrayList<>();
        namespaces.add(new MetadataRoutingContext.Reference.Namespace("ServiceRoot"));
        namespaces.add(new MetadataRoutingContext.Reference.Namespace("ServiceRoot.v1_15_0"));
        references.add(new MetadataRoutingContext.Reference(new URI("http://redfish.dmtf.org/schemas/v1/ServiceRoot_v1.xml"), namespaces));
        return references;
    }

    public String getServiceRootVersion() {
        return "v1_15_0";
    }
}
