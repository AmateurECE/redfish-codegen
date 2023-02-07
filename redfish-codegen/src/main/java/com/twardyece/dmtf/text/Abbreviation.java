package com.twardyece.dmtf.text;

public class Abbreviation implements IWord {
    private String abbreviation;
    public Abbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toUpperCase() {
        return this.abbreviation.toUpperCase();
    }

    @Override
    public String capitalize() {
        return toUpperCase();
    }

    @Override
    public String toLowerCase() {
        return this.abbreviation.toLowerCase();
    }
}
