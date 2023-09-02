package com.twardyece.dmtf.model.mapper;

import java.util.Optional;

/**
 * This interface is used to map models to other models. For example, to replace all instances of one model in the schema
 * with another, or map models within a namespace to models within another namespace. This is usually a one-way mapping,
 * in the sense that it's used to mutate and transform the Redfish Data Model before code generation.
 */
public interface IModelModelMapper {
    /**
     * Match one model to another
     * @param model The model name to match against
     * @return Optional.empty() if model does not match, else the name of a new model.
     */
    Optional<String> match(String model);
}
