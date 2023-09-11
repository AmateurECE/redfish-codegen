package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to map model names (e.g. in the form "Namespace_version_Model" to RustTypes. This is, by
 * definition, a two-way mapping, because there is a 1:1 correspondence between model names in the Redfish Data Model
 * and Rust types in the generated code.
 */
public interface IModelTypeMapper {
    /**
     * Map a model name to a rust type
     * @param name The model name
     * @return Optional.empty() if the model is not a match, else Optional.of(the new model).
     */
    Optional<ModelMatchSpecification> matchesType(String name);

    /**
     * Map a rust type to a model name. This is the inverse of matchesType
     * @param model A ModelMatchSpecification describing the model
     * @return Optional.of(the Redfish Data Model identifier for the model, if it matches a model).
     */
    Optional<String> matchesName(ModelMatchSpecification model);

    record ModelMatchSpecification(List<SnakeCaseName> path, PascalCaseName model) {}
}
