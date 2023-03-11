package com.twardyece.dmtf.identifiers;

public class IdentifierParseError extends RuntimeException {
    public IdentifierParseError(String message) {
        super(message);
    }
}
