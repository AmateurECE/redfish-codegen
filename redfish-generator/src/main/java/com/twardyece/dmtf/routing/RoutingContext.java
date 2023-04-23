package com.twardyece.dmtf.routing;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.RustIdentifier;
import com.twardyece.dmtf.RustType;

import java.util.List;

public class RoutingContext {
    public final ModuleContext moduleContext;
    public final RustType rustType;
    public final RustType routedTrait;
    public final List<Operation> operations;

    public RoutingContext(ModuleContext moduleContext, RustType rustType, RustType routedTrait, List<Operation> operations) {
        this.moduleContext = moduleContext;
        this.rustType = rustType;
        this.routedTrait = routedTrait;
        this.operations = operations;
    }

    public String name() { return this.rustType.toString(); }
    public String trait() { return this.routedTrait.toString(); }

    public static class Operation {
        public final String name;
        public final boolean mutable;
        public final RustIdentifier privilege;
        public final Body body;
        public final List<Parameter> parameters;
        public Operation(String name, boolean mutable, RustIdentifier privilege, Body body, List<Parameter> parameters) {
            this.name = name;
            this.mutable = mutable;
            this.privilege = privilege;
            this.body = body;
            this.parameters = parameters;
        }

        public boolean isPrivileged() { return null != this.privilege; }

        public record Body(String type) {}
        public record Parameter(String name, String type) {}
        public static class ResponseVariant {
            public final RustIdentifier rustIdentifier;
            public final RustIdentifier statusCode;
            public final List<Argument> arguments;
            public final List<Header> headers;

            public ResponseVariant(RustIdentifier rustIdentifier, RustIdentifier statusCode, List<Argument> arguments,
                                   List<Header> headers) {
                this.rustIdentifier = rustIdentifier;
                this.statusCode = statusCode;
                this.arguments = arguments;
                this.headers = headers;
            }

            public String identifier() { return this.rustIdentifier.toString(); }
            public boolean hasArgs() { return null != this.arguments && !this.arguments.isEmpty(); }
            public boolean hasHeaders() { return null != this.headers && !this.headers.isEmpty(); }

            public record Argument(String name) {}
            public record Header(String name, String content) {}
        }
    }
}
