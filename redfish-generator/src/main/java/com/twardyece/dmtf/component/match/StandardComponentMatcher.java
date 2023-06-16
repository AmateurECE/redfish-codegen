package com.twardyece.dmtf.component.match;

import com.twardyece.dmtf.component.ComponentContext;
import com.twardyece.dmtf.component.ComponentRepository;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Optional;

public class StandardComponentMatcher implements IComponentMatcher {
    public StandardComponentMatcher() {
    }

    @Override
    public Optional<ComponentContext> matchUri(ComponentRepository repository, String uri, PathItem pathItem) {
        Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();
        if (!operations.containsKey(PathItem.HttpMethod.GET)) {
            return Optional.empty();
        }

        // Perhaps a weak policy, but for now we assume that the component is the body type of the 200 response to
        // the GET request.
        MediaType mediaType = operations
                .get(PathItem.HttpMethod.GET)
                .getResponses()
                .get("200")
                .getContent()
                .get("application/json");
        if (null == mediaType) {
            return Optional.empty();
        }

        Schema schema = mediaType.getSchema();
        if (null == schema) {
            return Optional.empty();
        }

        ComponentContext context = repository.getOrCreateComponent(schema.get$ref());
        // TODO: Fill in stuff from PathItem here?
        return Optional.of(context);
    }
}
