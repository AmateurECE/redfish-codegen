package com.twardyece.dmtf;

import com.twardyece.dmtf.mapper.IModelFileMapper;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

public class ModelResolver {
    private IModelFileMapper[] mappers;

    public ModelResolver(IModelFileMapper[] mappers) {
        this.mappers = mappers;
    }

    public IModelFileMapper.ModelMatchResult resolve(String name, Schema schema) {
        for (IModelFileMapper mapper : this.mappers) {
            IModelFileMapper.ModelMatchResult module = mapper.matches(name, schema);
            if (null != module) {
                return module;
            }
        }

        return null;
    }
}
