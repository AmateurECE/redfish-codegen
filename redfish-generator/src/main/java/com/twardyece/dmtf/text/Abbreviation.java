package com.twardyece.dmtf.text;

import java.util.HashMap;
import java.util.Map;

public class Abbreviation implements IWord {
    private String upperCaseValue;
    public static final Map<String, IWord> SPECIAL_ABBREVIATIONS;

    static {
        SPECIAL_ABBREVIATIONS = new HashMap<>();
        SPECIAL_ABBREVIATIONS.put("PCIe", new Abbreviation("PCIe"));
        SPECIAL_ABBREVIATIONS.put("VLan", new Abbreviation("VLan"));
        SPECIAL_ABBREVIATIONS.put("VLANs", new Abbreviation("VLANs"));
        SPECIAL_ABBREVIATIONS.put("mUSB", new Word("mUSB"));
        SPECIAL_ABBREVIATIONS.put("uUSB", new Word("uUSB"));
        SPECIAL_ABBREVIATIONS.put("cSFP", new Word("cSFP"));
        SPECIAL_ABBREVIATIONS.put("IPv4", new Abbreviation("IPv4"));
        SPECIAL_ABBREVIATIONS.put("IPv6", new Abbreviation("IPv6"));
        SPECIAL_ABBREVIATIONS.put("kWh", new Abbreviation("kWh"));
        SPECIAL_ABBREVIATIONS.put("iSCSI", new Word("iSCSI"));
        SPECIAL_ABBREVIATIONS.put("NVMe", new Abbreviation("NVMe"));
        SPECIAL_ABBREVIATIONS.put("oF", new Abbreviation("oF"));
        SPECIAL_ABBREVIATIONS.put("OAuth2", new Abbreviation("OAuth2"));
        SPECIAL_ABBREVIATIONS.put("OAuth", new Abbreviation("OAuth"));
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
