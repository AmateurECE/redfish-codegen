package com.twardyece.dmtf;

import io.swagger.v3.oas.models.media.Schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedfishModelMapper implements IModelFileMapper {
    // The redfish document consistently names models of the form Module_vXX_XX_XX_Model
    private Pattern pattern;

    public RedfishModelMapper() {
        this.pattern = Pattern.compile("(?<module>[a-zA-z0-9]*)_(?<version>v[0-9]+_[0-9]+_[0-9]+)_(?<model>[a-zA-Z0-9]+)");
    }

    @Override
    public ModelFile matches(Schema model) {
        Matcher matcher = pattern.matcher(model.getName());
        if (!matcher.find()) {
            return null;
        }

        String[] module = new String[2];
        module[0] = matcher.group("module");
        module[1] = matcher.group("version");

        model.setName(matcher.group("model"));
        return new ModelFile(module, model);
    }
}
