package com.twardyece.dmtf.api;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.RustConfig;
import com.twardyece.dmtf.api.name.INameMapper;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.ArrayList;
import java.util.List;

public class EndpointResolver {
    private List<INameMapper> mappers;

    public EndpointResolver(List<INameMapper> mappers) { this.mappers = mappers; }

    public ApiMatchResult resolve(List<String> path) {
        List<SnakeCaseName> name = new ArrayList<>();
        for (String component : path) {
            boolean matched = false;
            for (INameMapper mapper : this.mappers) {
                SnakeCaseName result = mapper.matchComponent(component);
                if (null != result) {
                    matched = true;
                    name.add(result);
                    break;
                }
            }

            if (!matched) {
                throw new RuntimeException("No match for path component " + component);
            }
        }

        return new ApiMatchResult(name);
    }

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
