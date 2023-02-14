package com.twardyece.dmtf.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;

public interface IModelFileMapper {
    ModelMatchResult matches(String name);

    class ModelMatchResult {
        public ModelMatchResult(List<SnakeCaseName> path, PascalCaseName model) {
            path.add(0, RustConfig.MODELS_BASE_MODULE);
            this.path = CratePath.crateLocal(path);
            this.model = model;
        }

        public CratePath path;
        public PascalCaseName model;
    }
}
