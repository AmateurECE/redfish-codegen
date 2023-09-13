package com.twardyece.dmtf.model;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.model.mapper.NamespaceMapper;
import com.twardyece.dmtf.rust.RustConfig;
import com.twardyece.dmtf.rust.RustType;
import com.twardyece.dmtf.model.mapper.IModelTypeMapper;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelResolver {
    private final IModelTypeMapper[] mappers;
    private final NamespaceMapper[] namespaceMappers;
    private final RustTypeFactory rustTypeFactory;
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
        this.rustTypeFactory = new RustTypeFactory();
    }

    /**
     * Obtain just the schema identifier from the OpenAPI path. For example, converts
     * #/components/schemas/Message_v1_1_2_Message to Message_v1_1_2_Message. Throws RuntimeException if the Schema does
     * not contain a valid $ref.
     * @param schema The schema containing the path
     * @return the Redfish schema identifier.
     */
    public static String getSchemaIdentifier(Schema schema) {
        String url = schema.get$ref();
        // TODO: There's a faster way to do this than pattern matching.
        Matcher matcher = schemaPath.matcher(url);
        if (!matcher.find()) {
            throw new RuntimeException("Schema $ref path " + url + " appears to be malformed");
        }

        // Get the schema name
        return matcher.replaceFirst("");
    }

    /**
     * Use the injected IModelTypeMappers to resolve a Redfish Data Model identifier (in OpenAPI path format, such as
     * #/components/schemas/Message_v1_1_2_Message) to a Rust type.
     * @param name The OpenAPI path to resolve
     * @return The corresponding Rust type.
     */
    public RustType resolvePath(String name) {
        for (NamespaceMapper namespaceMapper : namespaceMappers) {
            Optional<String> match = namespaceMapper.match(name);
            if (match.isPresent()) {
                name = match.get();
            }
        }

        for (IModelTypeMapper mapper : this.mappers) {
            Optional<IModelTypeMapper.ModelMatchSpecification> module = mapper.matchesType(name);
            if (module.isPresent()) {
                return rustTypeFactory.toRustType(module.get());
            }
        }

        return null;
    }

    /**
     * Utilize the injected ModelTypeMappers to perform reverse resolution on a rust type.
     * @param rustType The rust type to resolve.
     * @return A string corresponding to the identifier used for this type in the Redfish Data Model.
     */
    public String reverseResolveIdentifier(RustType rustType) {
        // TODO: Today, namespace mapping is only necessary for forward resolution. This indicates a bit of potential
        //  fragility in the model resolution system that should be addressed.
        IModelTypeMapper.ModelMatchSpecification modelMatchSpecification = rustTypeFactory.toModelMatchSpecification(rustType);
        for (IModelTypeMapper mapper : this.mappers) {
            Optional<String> identifier = mapper.matchesName(modelMatchSpecification);
            if (identifier.isPresent()) {
                return identifier.get();
            }
        }

        return null;
    }

    /**
     * Resolve a Schema to a Rust type. If this schema represents a Redfish Data Model object, the corresponding type is
     * determined using resolvePath. If the Schema refers to a primitive type or a container type, this method correctly
     * resolves the type.
     * @param schema The schema to resolve.
     * @return A Rust type corresponding to the OpenAPI schema object.
     */
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

    /**
     * This little class encapsulates the logic of translating ModelMatchSpecification instances (from IModelTypeMapper)
     * to instances of RustType. It's not a complicated conversion, which is why it lives here. In the future, we can
     * move this class elsewhere if more configuration is required, so that it can be injected to the ModelResolver.
     */
    private class RustTypeFactory {
        public RustTypeFactory() {}

        /**
         *
         * @param matchResult The ModelMatchSpecification from an IModelTypeMapper
         * @return The corresponding RustType
         */
        public RustType toRustType(IModelTypeMapper.ModelMatchSpecification matchResult) {
            List<SnakeCaseName> path = matchResult.path();
            path.add(0, RustConfig.MODELS_BASE_MODULE);
            return new RustType(CratePath.crateLocal(path), matchResult.model());
        }

        /**
         *
         * @param rustType The RustType
         * @return A ModelMatchSpecification.
         */
        public IModelTypeMapper.ModelMatchSpecification toModelMatchSpecification(RustType rustType) {
            List<SnakeCaseName> path = rustType.getPath().getComponents();
            path.remove(0);
            return new IModelTypeMapper.ModelMatchSpecification(path, new PascalCaseName(rustType.getName()));
        }
    }
}
