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

use axum::{body::Body, Router, routing::get, Json};
use redfish_codegen::api::v1;
use redfish_codegen::models::{odata_v4, resource, service_root};

pub struct RedfishService(Router);

impl RedfishService {
    pub fn new() -> RedfishService {
        let router = Router::new().route("/redfish/v1", get(|| async {
            let response = <Self as v1::ServiceRoot>::get();
            match response {
                v1::ServiceRootGetResponse::Ok(service_root) => Ok(Json(service_root)),
                v1::ServiceRootGetResponse::Default(error) => Err(Json(error)),
            }
        }));
        RedfishService(router)
    }
}

impl v1::ServiceRoot for RedfishService {
    fn get() -> v1::ServiceRootGetResponse {
        v1::ServiceRootGetResponse::Ok(
            service_root::v1_15_0::ServiceRoot{
                name: resource::Name("Basic Redfish Service".to_string()),
                id: resource::Id("example-bin".to_string()),
                odata_id: odata_v4::Id("/redfish/v1".to_string()),
                ..Default::default()
            })
    }
}

impl Into<Router<(), Body>> for RedfishService {
    fn into(self) -> Router<(), Body> {
        self.0
    }
}
