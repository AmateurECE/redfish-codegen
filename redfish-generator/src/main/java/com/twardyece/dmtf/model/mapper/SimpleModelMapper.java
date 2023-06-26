package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.SimpleModelIdentifierFactory;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleModelMapper implements IModelFileMapper {
    private final SimpleModelIdentifierFactory identifierFactory;
    private final SnakeCaseName module;

    public SimpleModelMapper(SimpleModelIdentifierFactory identifierFactory, SnakeCaseName module) {
        this.identifierFactory = identifierFactory;
        this.module = module;
    }

    @Override
    public ModelMatchResult matches(String name) {
        Optional<PascalCaseName> model = this.identifierFactory.identify(name);
        if (model.isEmpty()) {
            return null;
        }

        List<SnakeCaseName> module = new ArrayList<>();
        module.add(this.module);

        return new ModelMatchResult(module, model.get());
    }
}
