package com.twardyece.dmtf.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PascalCaseName implements ICaseConvertible, Comparable<PascalCaseName> {
    ArrayList<IWord> words;
    private static final Pattern pascalCase = Pattern.compile("([A-Z][a-z]+)|([A-Z]+)(?=[A-Z][a-z])|([A-Z0-9]+)");

    public PascalCaseName(String name) {
        // PascalCase is a little harder than other cases. Since PascalCase strings may contain substrings that are not
        // in PascalCase, e.g. abbreviations like "PCIe", we have to intentionally handle those before attempting to
        // parse the identifier(s) as PascalCase.
        ArrayList<String> identifiers = new ArrayList<>();
        identifiers.add(name);
        for (String abbreviation : Abbreviation.SPECIAL_ABBREVIATIONS.keySet()) {
            for (int i = 0; i < identifiers.size(); ++i) {
                String identifier = identifiers.get(i);
                // We may run into a case where the current identifier is equal to the current abbreviation. There's
                // nothing we must do in that scenario.
                if (abbreviation.equals(identifier)) {
                    continue;
                }

                int index = identifier.indexOf(abbreviation);
                if (-1 != index) {
                    // Three cases:
                    //   1. It's at the beginning of the string (result is two strings)
                    if (0 == index) {
                        String first = identifier.substring(0, abbreviation.length());
                        String second = identifier.substring(abbreviation.length());
                        identifiers.set(i, first);
                        identifiers.add(i + 1, second);
                    }
                    //   2. It's at the end of the string (result is two strings)
                    else if (index + abbreviation.length() == identifier.length()) {
                        String first = identifier.substring(0, index);
                        String second = identifier.substring(index);
                        identifiers.set(i, first);
                        identifiers.add(i + 1, second);
                    }
                    //   3. It's in the middle of the string (result is three strings)
                    else {
                        String first = identifier.substring(0, index);
                        String second = identifier.substring(index, index + abbreviation.length());
                        String third = identifier.substring(index + abbreviation.length());
                        identifiers.set(i, first);
                        identifiers.add(i + 1, second);
                        identifiers.add(i + 2, third);
                    }
                }
            }
        }

        this.words = new ArrayList<>();
        for (String identifier : identifiers) {
            // Each identifier is either a special abbreviation, or a PascalCased string.
            if (Abbreviation.SPECIAL_ABBREVIATIONS.containsKey(identifier)) {
                this.words.add(Abbreviation.SPECIAL_ABBREVIATIONS.get(identifier));
            } else {
                parsePascalCaseName(identifier);
            }
        }
    }

    private void parsePascalCaseName(String name) {
        Matcher matcher = pascalCase.matcher(name);

        while (matcher.find()) {
            if (null != matcher.group(1)) {
                this.words.add(new Word(matcher.group(1)));
            } else if (null != matcher.group(2)) {
                this.words.add(new Abbreviation(matcher.group(2)));
            } else if (null != matcher.group(3)) {
                this.words.add(new Abbreviation(matcher.group(3)));
            } else {
                throw new CaseConversionError("PascalCase", name);
            }
        }

        if (this.words.size() == 0 && !"".equals(name)) {
            throw new CaseConversionError("PascalCase", name);
        }
    }

    public PascalCaseName(ICaseConvertible originalCase) {
        this.words = new ArrayList<>();
        this.words.addAll(originalCase.words());
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
    public int compareTo(PascalCaseName o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PascalCaseName) {
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
