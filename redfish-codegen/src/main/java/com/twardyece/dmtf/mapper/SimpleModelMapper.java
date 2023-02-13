package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.FileFactory;
import com.twardyece.dmtf.ModelFile;
import com.twardyece.dmtf.mapper.IModelFileMapper;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

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
    public ModelMatchResult matches(String name, Schema schema) {
        Matcher matcher = this.pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        SnakeCaseName[] module = new SnakeCaseName[1];
        module[0] = this.module;

        String model = matcher.group("model");
        return new ModelMatchResult(module, CaseConversion.toPascalCase(model));
    }
}
