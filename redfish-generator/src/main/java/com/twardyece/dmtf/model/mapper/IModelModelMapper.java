package com.twardyece.dmtf.model.mapper;

import java.util.Optional;

/**
 * This interface is used to map models to other models. For example, to replace all instances of one model in the schema
 * with another, or map models within a namespace to models within another namespace.
 */
public interface IModelModelMapper {
    /**
     * Match one model to another
     * @param model The model name to match against
     * @return Optional.empty() if model does not match, else the name of a new model.
     */
    Optional<String> match(String model);
}
