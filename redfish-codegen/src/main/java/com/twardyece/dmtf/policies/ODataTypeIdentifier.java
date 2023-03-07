package com.twardyece.dmtf.policies;

import com.twardyece.dmtf.identifiers.IdentifierParseError;
import com.twardyece.dmtf.identifiers.UnversionedSchemaIdentifier;
import com.twardyece.dmtf.identifiers.VersionedSchemaIdentifier;

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
