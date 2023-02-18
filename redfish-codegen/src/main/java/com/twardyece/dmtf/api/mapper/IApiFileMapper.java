package com.twardyece.dmtf.api.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.Collection;
import java.util.List;

public interface IApiFileMapper {
    ApiMatchResult matches(String name);

    public class ApiMatchResult {
        public ApiMatchResult(List<SnakeCaseName> path) {
            path.add(0, RustConfig.API_BASE_MODULE);
            this.path = CratePath.crateLocal(path);
        }

        CratePath path;
    }
}
