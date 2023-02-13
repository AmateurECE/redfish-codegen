package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedModelMapper implements IModelFileMapper {
    // The redfish document consistently names models of the form Module_vXX_XX_XX_Model
    private Pattern pattern;

    public VersionedModelMapper() {
        this.pattern = Pattern.compile("(?<module>[a-zA-z0-9]*)_(?<version>v[0-9]+_[0-9]+_[0-9]+)_(?<model>[a-zA-Z0-9]+)");
    }

    @Override
    public ModelMatchResult matches(String name, Schema schema) {
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        SnakeCaseName[] module = new SnakeCaseName[2];
        module[0] = new SnakeCaseName(new PascalCaseName(matcher.group("module")));
        module[1] = new SnakeCaseName(matcher.group("version"));

        String model = matcher.group("model");
        return new ModelMatchResult(module, new PascalCaseName(model));
    }
}
