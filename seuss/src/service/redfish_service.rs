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

use axum::{body::Body, extract::State, routing::get, Json, Router};
use redfish_codegen::api::v1::{self, ServiceRoot};

pub struct RedfishService(Router);

impl RedfishService {
    const URL: &str = "/redfish/v1";

    pub fn new<S>(state: S) -> RedfishService
    where
        S: ServiceRoot + Send + Sync + Clone + 'static,
    {
        let router = Router::new()
            .route(
                Self::URL,
                get(|State(state): State<S>| async move {
                    match state.get() {
                        v1::ServiceRootGetResponse::Ok(service_root) => Ok(Json(service_root)),
                        v1::ServiceRootGetResponse::Default(error) => Err(Json(error)),
                    }
                }),
            )
            .with_state(state);
        RedfishService(router)
    }
}

impl Into<Router<(), Body>> for RedfishService {
    fn into(self) -> Router<(), Body> {
        self.0
    }
}
