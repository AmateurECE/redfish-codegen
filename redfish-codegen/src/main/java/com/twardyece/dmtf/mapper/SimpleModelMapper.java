package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.FileFactory;
import com.twardyece.dmtf.ModelFile;
import com.twardyece.dmtf.mapper.IModelFileMapper;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleModelMapper implements IModelFileMapper {
    private Pattern pattern;
    private SnakeCaseName module;
    private FileFactory factory;

    public SimpleModelMapper(Pattern regex, SnakeCaseName module, FileFactory factory) {
        this.pattern = regex;
        this.module = module;
        this.factory = factory;
    }

    @Override
    public ModelFile matches(Schema model) {
        Matcher matcher = this.pattern.matcher(model.getName());
        if (!matcher.find()) {
            return null;
        }

        SnakeCaseName[] module = new SnakeCaseName[1];
        module[0] = this.module;

        model.setName(matcher.group("model"));
        return this.factory.makeModelFile(module, model);
    }
}
