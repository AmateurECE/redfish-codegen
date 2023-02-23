package com.twardyece.dmtf.api;

import com.twardyece.dmtf.api.mapper.IApiFileMapper;

public class EndpointResolver {
    private IApiFileMapper[] mappers;

    public EndpointResolver(IApiFileMapper[] mappers) { this.mappers = mappers; }

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
