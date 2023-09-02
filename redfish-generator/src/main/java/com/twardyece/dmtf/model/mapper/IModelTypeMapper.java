package com.twardyece.dmtf.model.mapper;

import com.twardyece.dmtf.CratePath;
import com.twardyece.dmtf.rust.RustConfig;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to map model names (e.g. in the form "Namespace_version_Model" to RustTypes.
 */
public interface IModelTypeMapper {
    /**
     * Map a model name to a rust type
     * @param name The model name
     * @return Optional.empty() if the model is not a match, else Optional.of(the new model).
     */
    Optional<ModelMatchSpecification> matchesType(String name);

    class ModelMatchSpecification {
        public ModelMatchSpecification(List<SnakeCaseName> path, PascalCaseName model) {
            path.add(0, RustConfig.MODELS_BASE_MODULE);
            this.path = CratePath.crateLocal(path);
            this.model = model;
        }

        public CratePath path;
        public PascalCaseName model;
    }
}
