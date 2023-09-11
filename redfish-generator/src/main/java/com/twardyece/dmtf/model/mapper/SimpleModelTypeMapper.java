package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.specification.SimpleModelIdentifierFactory;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleModelTypeMapper implements IModelTypeMapper {
    private final SimpleModelIdentifierFactory identifierFactory;
    private final SnakeCaseName module;

    public SimpleModelTypeMapper(SimpleModelIdentifierFactory identifierFactory, SnakeCaseName module) {
        this.identifierFactory = identifierFactory;
        this.module = module;
    }

    @Override
    public Optional<ModelMatchSpecification> matchesType(String name) {
        Optional<PascalCaseName> model = this.identifierFactory.modelName(name);
        if (model.isEmpty()) {
            return Optional.empty();
        }

        List<SnakeCaseName> module = new ArrayList<>();
        module.add(this.module);

        return Optional.of(new ModelMatchSpecification(module, model.get()));
    }

    @Override
    public Optional<String> matchesName(ModelMatchSpecification model) {
        if (model.path().isEmpty()) {
            return Optional.empty();
        }

        SnakeCaseName module = model.path().get(model.path().size() - 1);
        if (module != this.module) {
            return Optional.empty();
        }

        return Optional.of(this.identifierFactory.schemaIdentifier(model.model()));
    }
}
