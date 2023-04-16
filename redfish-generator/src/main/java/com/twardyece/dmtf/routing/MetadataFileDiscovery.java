package com.twardyece.dmtf.routing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetadataFileDiscovery {
    private final Path csdlDirectory;
    private final DocumentBuilderFactory factory;

    public MetadataFileDiscovery(Path csdlDirectory) {
        this.csdlDirectory = csdlDirectory;
        this.factory = DocumentBuilderFactory.newInstance();
    }

    public List<MetadataRoutingContext.Reference> getReferences() throws URISyntaxException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = this.factory.newDocumentBuilder();
        List<MetadataRoutingContext.Reference> references = new ArrayList<>();
        for (String file : Objects.requireNonNull(this.csdlDirectory.toFile().list())) {
            Document document = builder.parse(this.csdlDirectory + "/" + file);
            NodeList schemas = document.getElementsByTagName("Schema");

            List<MetadataRoutingContext.Reference.Namespace> namespaces = new ArrayList<>();
            if (1 > schemas.getLength()) {
                throw new RuntimeException("No Schema tags present in CSDL file " + file);
            } else if (1 == schemas.getLength()) {
                // If there's only one Schema object, this might be an unversioned schema, like ComputerSystemCollection.
                namespaces.add(getNamespaceFromNode(schemas.item(0)));
            } else {
                // Otherwise, get the first schema (which is usually the parent one) and the last (the latest version).
                namespaces.add(getNamespaceFromNode(schemas.item(0)));
                namespaces.add(getNamespaceFromNode(schemas.item(schemas.getLength() - 1)));
            }

            URI uri = new URI("http://redfish.dmtf.org/schemas/v1/" + file);
            references.add(new MetadataRoutingContext.Reference(uri, namespaces));
        }
        return references;
    }

    private static MetadataRoutingContext.Reference.Namespace getNamespaceFromNode(Node item) {
        Element element = (Element)item;
        return new MetadataRoutingContext.Reference.Namespace(element.getAttribute("Namespace"));
    }

    public String getServiceRootVersion() {
        return "v1_15_0";
    }
}
