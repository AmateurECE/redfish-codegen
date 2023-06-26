package com.twardyece.dmtf.specification;

public class ODataTypeIdentifier {
    public ODataTypeIdentifier() {}

    public String identify(String identifier) {
        try {
            VersionedSchemaIdentifier versioned = new VersionedSchemaIdentifier(identifier);
            return "#" + versioned.getModule() + "." + versioned.getVersion() + "." + versioned.getModel();
        } catch (IdentifierParseError e) {
            UnversionedSchemaIdentifier unversioned = new UnversionedSchemaIdentifier(identifier);
            return "#" + unversioned.getModule() + "." + unversioned.getModel();
        }
    }
}
