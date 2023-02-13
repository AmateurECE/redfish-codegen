package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;

public interface IModelFileMapper {
    ModelMatchResult matches(String name, Schema model);

    class ModelMatchResult {
        public ModelMatchResult(SnakeCaseName[] path, PascalCaseName model) {
            this.path = path;
            this.model = model;
        }

        public SnakeCaseName[] path;
        public PascalCaseName model;
    }
}
