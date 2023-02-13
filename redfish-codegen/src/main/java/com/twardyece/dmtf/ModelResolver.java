package com.twardyece.dmtf;

import com.twardyece.dmtf.mapper.IModelFileMapper;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelResolver {
    private IModelFileMapper[] mappers;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelResolver.class);
    private static final SnakeCaseName[] VEC_PATH;
    private static final PascalCaseName VEC_NAME = new PascalCaseName("Vec");
    private static final Map<String, RustType> RUST_TYPE_MAP;
    private static final Pattern schemaPath = Pattern.compile("#/components/schemas/");

    static {
        HashMap<String, RustType> typeMap = new HashMap<>();
        typeMap.put("integer", new RustType(new SnakeCaseName("i64")));
        typeMap.put("boolean", new RustType(new SnakeCaseName("bool")));
        typeMap.put("number", new RustType(new SnakeCaseName("f64")));

        SnakeCaseName[] stringPath = new SnakeCaseName[2];
        stringPath[0] = new SnakeCaseName("std");
        stringPath[1] = new SnakeCaseName("string");
        typeMap.put("string", new RustType(stringPath, new PascalCaseName("String")));

        RUST_TYPE_MAP = Collections.unmodifiableMap(typeMap);

        VEC_PATH = new SnakeCaseName[2];
        VEC_PATH[0] = new SnakeCaseName("std");
        VEC_PATH[1] = new SnakeCaseName("vec");
    }


    public ModelResolver(IModelFileMapper[] mappers) {
        this.mappers = mappers;
    }

    public IModelFileMapper.ModelMatchResult resolve(String name) {
        for (IModelFileMapper mapper : this.mappers) {
            IModelFileMapper.ModelMatchResult module = mapper.matches(name);
            if (null != module) {
                return module;
            }
        }

        return null;
    }

    // Resolve an OpenAPI path, such as '#/components/schemas/RedfishError' to a RustType.
    public RustType resolveType(Schema schema) {
        String type = schema.getType();
        if (null == type) {
            // It's an object with a $ref
            String url = schema.get$ref();
            Matcher matcher = schemaPath.matcher(url);
            if (!matcher.find()) {
                LOGGER.error("Schema $ref path " + url + " appears to be malformed");
                return null;
            }

            // Get the schema name
            String name = matcher.replaceFirst("");
            IModelFileMapper.ModelMatchResult result = this.resolve(name);
            return new RustType(result.path, result.model);
        } else if ("array".equals(schema.getType())) {
            // It's an array type
            return new RustType(VEC_PATH, VEC_NAME, this.resolveType(schema.getItems()));
        } else {
            if (!RUST_TYPE_MAP.containsKey(type)) {
                LOGGER.warn("No mapping for type " + type);
            }
            return RUST_TYPE_MAP.get(type);
        }
    }
}
