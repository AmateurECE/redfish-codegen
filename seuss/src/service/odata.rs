// Author: Ethan D. Twardy <ethan.twardy@gmail.com>
//
// Copyright 2023, Ethan Twardy. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the \"License\");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an \"AS IS\" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

use axum::{
    extract::State,
    routing::{self, MethodRouter},
    Json,
};
use redfish_codegen::models::odata_v4::{Context, Service, ServiceDocument};

/// This endpoint implements the OData Service Document.
#[derive(Clone, Default)]
pub struct OData(ServiceDocument);

impl OData {
    pub fn new() -> Self {
        Self(ServiceDocument {
            odata_context: Context("/redfish/v1/$metadata".to_string()),
            value: vec![Service {
                name: "Service".to_string(),
                url: "/redfish/v1/".to_string(),
                ..Default::default()
            }],
        })
    }

    pub fn enable_systems(mut self) -> Self {
        self.0.value.push(Service {
            name: "Systems".to_string(),
            url: "/redfish/v1/Systems".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_chassis(mut self) -> Self {
        self.0.value.push(Service {
            name: "Chassis".to_string(),
            url: "/redfish/v1/Chassis".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_managers(mut self) -> Self {
        self.0.value.push(Service {
            name: "Managers".to_string(),
            url: "/redfish/v1/Managers".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_task_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "TaskService".to_string(),
            url: "/redfish/v1/TaskService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_account_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "AccountService".to_string(),
            url: "/redfish/v1/AccountService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_session_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "SessionService".to_string(),
            url: "/redfish/v1/SessionService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_event_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "EventService".to_string(),
            url: "/redfish/v1/EventService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_registries(mut self) -> Self {
        self.0.value.push(Service {
            name: "Registries".to_string(),
            url: "/redfish/v1/Registries".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_json_schemas(mut self) -> Self {
        self.0.value.push(Service {
            name: "JsonSchemas".to_string(),
            url: "/redfish/v1/JsonSchemas".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_certificate_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "CertificateService".to_string(),
            url: "/redfish/v1/CertificateService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_key_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "KeyService".to_string(),
            url: "/redfish/v1/KeyService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_update_service(mut self) -> Self {
        self.0.value.push(Service {
            name: "UpdateService".to_string(),
            url: "/redfish/v1/UpdateService".to_string(),
            ..Default::default()
        });
        self
    }

    pub fn enable_sessions(mut self) -> Self {
        self.0.value.push(Service {
            name: "Sessions".to_string(),
            url: "/redfish/v1/SessionService/Sessions".to_string(),
            ..Default::default()
        });
        self
    }
}

impl Into<MethodRouter> for OData {
    fn into(self) -> MethodRouter {
        routing::get(|State(odata): State<OData>| async move { Json(odata.0) }).with_state(self)
    }
}
