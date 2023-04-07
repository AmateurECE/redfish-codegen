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

use axum::{extract::State, routing, Json};
use redfish_codegen::api::v1;

pub struct ServiceRoot(routing::MethodRouter);

impl ServiceRoot {
    pub fn new<S>(state: S) -> ServiceRoot
    where
        S: v1::ServiceRoot + Send + Sync + Clone + 'static,
    {
        let router = routing::get(|State(state): State<S>| async move {
            match state.get() {
                v1::ServiceRootGetResponse::Ok(service_root) => Ok(Json(service_root)),
                v1::ServiceRootGetResponse::Default(error) => Err(Json(error)),
            }
        })
        .with_state(state);
        ServiceRoot(router)
    }
}

impl Into<routing::MethodRouter> for ServiceRoot {
    fn into(self) -> routing::MethodRouter {
        self.0
    }
}
