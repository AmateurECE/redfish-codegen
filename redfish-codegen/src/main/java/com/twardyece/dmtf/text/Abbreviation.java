package com.twardyece.dmtf.text;

import java.util.HashMap;
import java.util.Map;

public class Abbreviation implements IWord {
    private String upperCaseValue;
    public static final Map<String, Abbreviation> SPECIAL_ABBREVIATIONS;

    static {
        SPECIAL_ABBREVIATIONS = new HashMap<>();
        SPECIAL_ABBREVIATIONS.put("PCIe", new Abbreviation("PCIe"));
        SPECIAL_ABBREVIATIONS.put("VLan", new Abbreviation("VLan"));
    }

    public Abbreviation(String upperCaseValue) {
        this.upperCaseValue = upperCaseValue;
    }

    @Override
    public String toUpperCase() { return this.upperCaseValue; }

    @Override
    public String capitalize() {
        return upperCaseValue;
    }

    @Override
    public String toLowerCase() {
        return this.upperCaseValue.toLowerCase();
    }
}
