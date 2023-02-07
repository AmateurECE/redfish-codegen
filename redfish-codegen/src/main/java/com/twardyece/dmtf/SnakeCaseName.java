package com.twardyece.dmtf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnakeCaseName implements ICaseConvertible {
    private ArrayList<IWord> words;

    private static final Pattern snakeCase = Pattern.compile("([a-z0-9]+)");

    public SnakeCaseName(ICaseConvertible originalCase) {
        this.words = new ArrayList<>();
        this.words.addAll(originalCase.words());
    }

    public SnakeCaseName(String name) {
        this.words = new ArrayList<>();
        Matcher matcher = snakeCase.matcher(name);
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
}
