package com.twardyece.dmtf.text;

import com.twardyece.dmtf.text.IWord;

public class Word implements IWord {
    private String word;
    public Word(String word) {
        this.word = word;
    }

    @Override
    public String toUpperCase() {
        return this.word.toUpperCase();
    }

    @Override
    public String capitalize() {
        String word = this.word.toLowerCase();
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    @Override
    public String toLowerCase() {
        return this.word.toLowerCase();
    }
}
