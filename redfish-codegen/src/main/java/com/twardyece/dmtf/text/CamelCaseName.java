package com.twardyece.dmtf.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelCaseName implements ICaseConvertible, Comparable<CamelCaseName> {
    List<IWord> words;
    private static final Pattern camelCase = Pattern.compile("(?<prefix>^[a-z0-9]+)");

    public CamelCaseName(String name) {
        // Camel Case names are just PascalCaseNames with a lowercase word prefix.
        Matcher matcher = camelCase.matcher(name);
        if (!matcher.find()) {
            throw new ICaseConvertible.CaseConversionError("camelCase", name);
        }

        this.words = new ArrayList<>();
        words.add(new Word(matcher.group("prefix")));
        words.addAll(new PascalCaseName(matcher.replaceAll("")).words());
    }

    @Override
    public Collection<? extends IWord> words() {
        return this.words;
    }

    @Override
    public String toString() {
        String value = "";
        for (IWord word : this.words) {
            value += word.capitalize();
        }
        return value;
    }

    @Override
    public int compareTo(CamelCaseName o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CamelCaseName) {
            return this.toString().equals(o.toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
