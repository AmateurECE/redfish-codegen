package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnversionedModelMapper implements IModelFileMapper {
    // The redfish document consistently names models of the form Module_vXX_XX_XX_Model
    private Pattern pattern;

    public UnversionedModelMapper() {
        this.pattern = Pattern.compile("(?<module>[a-zA-z0-9]*)_(?<model>[a-zA-Z0-9]+)");
    }

    @Override
    public ModelMatchResult matches(String name) {
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        List<SnakeCaseName> module = new ArrayList<>();
        module.add(CaseConversion.toSnakeCase(matcher.group("module")));

        String model = matcher.group("model");
        return new ModelMatchResult(module, new PascalCaseName(model));
    }
}
