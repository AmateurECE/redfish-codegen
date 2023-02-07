package com.twardyece.dmtf.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PascalCasedName implements ICaseConvertible {
    ArrayList<IWord> words;
    private static final Pattern pascalCase = Pattern.compile("([A-Z][a-z][a-z]+)|([A-Z]+)(?=[A-Z][a-z][a-z])|([A-Z0-9]+)");

    public PascalCasedName(String name) {
        this.words = new ArrayList<>();
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

    public PascalCasedName(ICaseConvertible originalCase) {
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
}
