package com.twardyece.dmtf.api.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.text.CaseConversion;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.Collection;
import java.util.List;

public interface IApiFileMapper {
    ApiMatchResult matches(String name);

    class ApiMatchResult {
        public ApiMatchResult(List<SnakeCaseName> path) {
            path.add(0, RustConfig.API_BASE_MODULE);
            this.path = CratePath.crateLocal(path);
            this.name = new PascalCaseName(path.get(path.size() - 1));
        }

        public CratePath path;
        public PascalCaseName name;
    }
}
