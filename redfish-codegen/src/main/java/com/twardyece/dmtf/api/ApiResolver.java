package com.twardyece.dmtf.api;

import com.twardyece.dmtf.api.mapper.IApiFileMapper;
import com.twardyece.dmtf.model.mapper.IModelFileMapper;

public class ApiResolver {
    private IApiFileMapper[] mappers;

    public ApiResolver(IApiFileMapper[] mappers) { this.mappers = mappers; }

    public IApiFileMapper.ApiMatchResult resolve(String name) {
        for (IApiFileMapper mapper : this.mappers) {
            IApiFileMapper.ApiMatchResult module = mapper.matches(name);
            if (null != module) {
                return module;
            }
        }

        return null;
    }
}
