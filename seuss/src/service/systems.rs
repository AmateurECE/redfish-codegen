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

use crate::endpoint::Endpoint;
use axum::{extract::State, routing::get, Json, Router};
use redfish_codegen::api::v1::systems;

pub struct Systems(Router);

impl Systems {
    pub fn new<S>(state: S) -> Self
    where
        S: systems::Systems + Endpoint + Send + Sync + Clone + 'static,
    {
        let router = Router::new()
            .route(
                "/",
                get(|State(state): State<S>| async move {
                    match state.get() {
                        systems::SystemsGetResponse::Ok(collection) => Ok(Json(collection)),
                        systems::SystemsGetResponse::Default(error) => Err(Json(error)),
                    }
                }),
            )
            .with_state(state);
        Systems(router)
    }
}

impl Into<Router> for Systems {
    fn into(self) -> Router {
        self.0
    }
}
