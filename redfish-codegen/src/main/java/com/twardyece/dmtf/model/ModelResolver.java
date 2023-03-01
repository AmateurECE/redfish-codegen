package com.twardyece.dmtf.model;

import com.twardyece.dmtf.RustType;
import com.twardyece.dmtf.model.mapper.IModelFileMapper;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelResolver {
    private IModelFileMapper[] mappers;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelResolver.class);
    private static final PascalCaseName VEC_NAME = new PascalCaseName("Vec");
    private static final Map<String, RustType> RUST_TYPE_MAP;
    private static final Pattern schemaPath = Pattern.compile("#/components/schemas/");

    static {
        RUST_TYPE_MAP = new HashMap<>();
        RUST_TYPE_MAP.put("integer", new RustType(new SnakeCaseName("i64")));
        RUST_TYPE_MAP.put("boolean", new RustType(new SnakeCaseName("bool")));
        RUST_TYPE_MAP.put("number", new RustType(new SnakeCaseName("f64")));
        RUST_TYPE_MAP.put("string", new RustType(null, new PascalCaseName("String")));
    }


    public ModelResolver(IModelFileMapper[] mappers) {
        this.mappers = mappers;
    }

    public RustType resolvePath(String name) {
        for (IModelFileMapper mapper : this.mappers) {
            IModelFileMapper.ModelMatchResult module = mapper.matches(name);
            if (null != module) {
                return new RustType(module.path, module.model);
            }
        }

        return null;
    }

    // Resolve an OpenAPI path, such as '#/components/schemas/RedfishError' to a RustType.
    public RustType resolveSchema(Schema schema) {
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
            return this.resolvePath(name);
        } else if ("array".equals(schema.getType())) {
            // It's an array type
            return new RustType(null, VEC_NAME, this.resolveSchema(schema.getItems()));
        } else {
            if (!RUST_TYPE_MAP.containsKey(type)) {
                LOGGER.warn("No mapping for type " + type);
            }
            return RUST_TYPE_MAP.get(type);
        }
    }
}
