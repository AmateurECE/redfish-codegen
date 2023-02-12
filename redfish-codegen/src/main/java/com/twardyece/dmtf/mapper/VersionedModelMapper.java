package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.FileFactory;
import com.twardyece.dmtf.ModelFile;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedModelMapper implements IModelFileMapper {
    // The redfish document consistently names models of the form Module_vXX_XX_XX_Model
    private Pattern pattern;
    private FileFactory factory;

    public VersionedModelMapper(FileFactory factory) {
        this.pattern = Pattern.compile("(?<module>[a-zA-z0-9]*)_(?<version>v[0-9]+_[0-9]+_[0-9]+)_(?<model>[a-zA-Z0-9]+)");
        this.factory = factory;
    }

    @Override
    public ModelFile matches(Schema model) {
        Matcher matcher = pattern.matcher(model.getName());
        if (!matcher.find()) {
            return null;
        }

        SnakeCaseName[] module = new SnakeCaseName[2];
        module[0] = new SnakeCaseName(new PascalCaseName(matcher.group("module")));
        module[1] = new SnakeCaseName(matcher.group("version"));

        model.setName(matcher.group("model"));
        return this.factory.makeModelFile(module, model);
    }
}
