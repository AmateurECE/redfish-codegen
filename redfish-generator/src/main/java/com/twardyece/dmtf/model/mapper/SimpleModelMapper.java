package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleModelMapper implements IModelFileMapper {
    private Pattern pattern;
    private SnakeCaseName module;

    public SimpleModelMapper(Pattern regex, SnakeCaseName module) {
        this.pattern = regex;
        this.module = module;
    }

    @Override
    public ModelMatchResult matches(String name) {
        Matcher matcher = this.pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        List<SnakeCaseName> module = new ArrayList<>();
        module.add(this.module);

        String model = matcher.group("model");
        return new ModelMatchResult(module, CaseConversion.toPascalCase(model));
    }
}
