package com.twardyece.dmtf;

import io.swagger.v3.oas.models.media.Schema;

public interface IModelFileMapper {
    public abstract ModelFile matches(Schema model);
}
