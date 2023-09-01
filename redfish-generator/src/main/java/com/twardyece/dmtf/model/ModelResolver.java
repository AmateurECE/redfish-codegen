package com.twardyece.dmtf.model;

import com.twardyece.dmtf.model.mapper.NamespaceMapper;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.model.mapper.IModelTypeMapper;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelResolver {
    private final IModelTypeMapper[] mappers;
    private final NamespaceMapper[] namespaceMappers;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelResolver.class);
    private static final PascalCaseName VEC_NAME = new PascalCaseName("Vec");
    public static final Map<String, RustType> RUST_TYPE_MAP;
    private static final Pattern schemaPath = Pattern.compile("#/components/schemas/");

    static {
        RUST_TYPE_MAP = new HashMap<>();
        RUST_TYPE_MAP.put("integer", new RustType(new SnakeCaseName("i64")));
        RUST_TYPE_MAP.put("boolean", new RustType(new SnakeCaseName("bool")));
        RUST_TYPE_MAP.put("number", new RustType(new SnakeCaseName("f64")));
        RUST_TYPE_MAP.put("string", new RustType(null, new PascalCaseName("String")));
    }


    public ModelResolver(IModelTypeMapper[] mappers, NamespaceMapper[] namespaceMappers) {
        this.mappers = mappers;
        this.namespaceMappers = namespaceMappers;
    }

    public static String getSchemaIdentifier(Schema schema) {
        String url = schema.get$ref();
        Matcher matcher = schemaPath.matcher(url);
        if (!matcher.find()) {
            throw new RuntimeException("Schema $ref path " + url + " appears to be malformed");
        }

        // Get the schema name
        return matcher.replaceFirst("");
    }

    public RustType resolvePath(String name) {
        for (NamespaceMapper namespaceMapper : namespaceMappers) {
            Optional<String> match = namespaceMapper.match(name);
            if (match.isPresent()) {
                name = match.get();
            }
        }

        for (IModelTypeMapper mapper : this.mappers) {
            Optional<IModelTypeMapper.ModelMatchResult> module = mapper.matches(name);
            if (module.isPresent()) {
                return new RustType(module.get().path, module.get().model);
            }
        }

        return null;
    }

    // Resolve an OpenAPI path, such as '#/components/schemas/RedfishError' to a RustType.
    public RustType resolveSchema(Schema schema) {
        String type = schema.getType();
        if (null == type) {
            return this.resolvePath(getSchemaIdentifier(schema));
        } else if ("array".equals(schema.getType())) {
            // It's an array type
            return new RustType(null, VEC_NAME, new RustType[]{this.resolveSchema(schema.getItems())});
        } else {
            if (!RUST_TYPE_MAP.containsKey(type)) {
                LOGGER.warn("No mapping for type " + type);
            }
            return RUST_TYPE_MAP.get(type);
        }
    }
}
