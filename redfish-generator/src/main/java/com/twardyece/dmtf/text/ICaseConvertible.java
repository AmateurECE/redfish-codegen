package com.twardyece.dmtf.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface ICaseConvertible {
    public Collection<? extends IWord> words();

    class CaseConversionError extends RuntimeException {
        public CaseConversionError(String targetCase, String text) {
            super("String " + text + " is not convertible to " + targetCase);
        }
    }
}
