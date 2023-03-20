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

use crate::auth::{ConfigureComponents, Login};
use crate::extract::RedfishAuth;
use axum::{extract::State, http::StatusCode, response::IntoResponse, routing, Json};
use redfish_codegen::api::v1::systems;
use redfish_codegen::models::computer_system::v1_20_0::ComputerSystem;

pub struct Systems(routing::MethodRouter);

impl Systems {
    pub fn new<S>(state: S) -> Self
    where
        S: systems::Systems + Send + Sync + Clone + 'static,
    {
        let router = routing::get(|State(state): State<S>, _: RedfishAuth<Login>| async move {
            match state.get() {
                systems::SystemsGetResponse::Ok(collection) => Ok(Json(collection)),
                systems::SystemsGetResponse::Default(error) => Err(Json(error)),
            }
        })
        .post(
            |State(mut state): State<S>,
             _: RedfishAuth<ConfigureComponents>,
             Json(body): Json<ComputerSystem>| async move {
                match state.post(body) {
                    systems::SystemsPostResponse::Created(computer_system) => {
                        (StatusCode::CREATED, Json(computer_system)).into_response()
                    }
                    systems::SystemsPostResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    systems::SystemsPostResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    systems::SystemsPostResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .with_state(state);
        Systems(router)
    }
}

impl Into<routing::MethodRouter> for Systems {
    fn into(self) -> routing::MethodRouter {
        self.0
    }
}
