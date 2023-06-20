package com.twardyece.dmtf.component;

import com.twardyece.dmtf.ModuleContext;
import com.twardyece.dmtf.text.PascalCaseName;
import com.twardyece.dmtf.text.SnakeCaseName;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

// Context for the OData Service Document routes.
public class ODataContext {
    // TODO: This module depends on a few models in redfish_codegen. Should inject their paths here instead of including
    // them in the template.
    public static final List<Service> services;
    public static final String odataContext = "/redfish/v1/$metadata";
    private final ModuleContext moduleContext;

    static {
        services = new ArrayList<>();
        try {
            // TODO: Should determine these dynamically from the list of URLs instead of hard-coding them here.
            services.add(new Service(new PascalCaseName("Systems"), new URI("/redfish/v1/Systems")));
            services.add(new Service(new PascalCaseName("Chassis"), new URI("/redfish/v1/Chassis")));
            services.add(new Service(new PascalCaseName("Managers"), new URI("/redfish/v1/Managers")));
            services.add(new Service(new PascalCaseName("TaskService"), new URI("/redfish/v1/TaskService")));
            services.add(new Service(new PascalCaseName("AccountService"), new URI("/redfish/v1/AccountService")));
            services.add(new Service(new PascalCaseName("SessionService"), new URI("/redfish/v1/SessionService")));
            services.add(new Service(new PascalCaseName("EventService"), new URI("/redfish/v1/EventService")));
            services.add(new Service(new PascalCaseName("Registries"), new URI("/redfish/v1/Registries")));
            services.add(new Service(new PascalCaseName("JsonSchemas"), new URI("/redfish/v1/JsonSchemas")));
            services.add(new Service(new PascalCaseName("CertificateService"), new URI("/redfish/v1/CertificateService")));
            services.add(new Service(new PascalCaseName("KeyService"), new URI("/redfish/v1/KeyService")));
            services.add(new Service(new PascalCaseName("UpdateService"), new URI("/redfish/v1/UpdateService")));
            services.add(new Service(new PascalCaseName("Sessions"), new URI("/redfish/v1/SessionService/Sessions")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ODataContext(ModuleContext module) {
        this.moduleContext = module;
    }

    public ModuleContext module() { return this.moduleContext; }

    public static class Service {
        private final PascalCaseName serviceName;
        private final URI uri;

        public Service(PascalCaseName serviceName, URI uri) {
            this.serviceName = serviceName;
            this.uri = uri;
        }

        public String name() {
            return this.serviceName.toString();
        }

        public String service() {
            SnakeCaseName name = new SnakeCaseName(this.serviceName);
            return name.toString();
        }

        public String uri() {
            return this.uri.toString();
        }
    }
}
