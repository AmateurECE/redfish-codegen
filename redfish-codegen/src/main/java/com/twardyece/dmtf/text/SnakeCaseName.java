package com.twardyece.dmtf.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SnakeCaseName implements ICaseConvertible, Comparable<SnakeCaseName> {
    private ArrayList<IWord> words;

    private static final Pattern snakeCase = Pattern.compile("([a-z0-9]+)");
    private static final Pattern uppercase = Pattern.compile("[A-Z]");

    public SnakeCaseName(ICaseConvertible originalCase) {
        this.words = new ArrayList<>();
        this.words.addAll(originalCase.words());
    }

    public SnakeCaseName(Collection<? extends ICaseConvertible> identifiers) {
        this.words = new ArrayList<>();
        identifiers.stream().forEach((identifier) -> this.words.addAll(identifier.words()));
    }

    public SnakeCaseName(String name) {
        // Since our snakeCase regex will match even if the string contains uppercase characters, we have to check
        // whether there are any first.
        Matcher matcher = uppercase.matcher(name);
        if (matcher.find()) {
            throw new CaseConversionError("snake_case", name);
        }

        this.words = new ArrayList<>();
        matcher = snakeCase.matcher(name);
        while (matcher.find()) {
            if (null == matcher.group(1)) {
                throw new CaseConversionError("snake_case", name);
            }
            this.words.add(new Word(matcher.group(1)));
        }

        if (this.words.size() == 0 && !"".equals(name)) {
            throw new CaseConversionError("snake_case", name);
        }
    }

    @Override
    public Collection<? extends IWord> words() {
        return this.words;
    }

    @Override
    public String toString() {
        ArrayList<String> words = new ArrayList<>();
        for (IWord word : this.words) {
            words.add(word.toLowerCase());
        }

        return String.join("_", words);
    }

    @Override
    public int compareTo(SnakeCaseName o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SnakeCaseName) {
            return this.toString().equals(o.toString());
        } else {
            return false;
        }
    }
}
