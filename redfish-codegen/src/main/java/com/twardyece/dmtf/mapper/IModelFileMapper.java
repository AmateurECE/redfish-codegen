package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.ModelFile;
import io.swagger.v3.oas.models.media.Schema;

public interface IModelFileMapper {
    public abstract ModelFile matches(Schema model);
}
