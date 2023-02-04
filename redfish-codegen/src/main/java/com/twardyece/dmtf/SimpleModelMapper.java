package com.twardyece.dmtf;

import io.swagger.v3.oas.models.media.Schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleModelMapper implements IModelFileMapper {
    Pattern pattern;
    String module;

    public SimpleModelMapper(Pattern regex, String module) {
        this.pattern = regex;
        this.module = module;
    }

    @Override
    public ModelFile matches(Schema model) {
        Matcher matcher = this.pattern.matcher(model.getName());
        if (!matcher.find()) {
            return null;
        }

        String[] module = new String[1];
        module[0] = this.module;

        model.setName(matcher.group("model"));
        return new ModelFile(module, model);
    }
}
