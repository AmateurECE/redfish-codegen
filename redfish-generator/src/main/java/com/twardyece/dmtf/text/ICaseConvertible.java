package com.twardyece.dmtf.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface ICaseConvertible {
    public Collection<? extends IWord> words();

    public class CaseConversionError extends RuntimeException {
        private String targetCase;
        private String text;

        public CaseConversionError(String targetCase, String text) {
            super("String " + text + " is not convertible to " + targetCase);
        }
    }
}
