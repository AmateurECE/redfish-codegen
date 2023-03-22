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

use crate::auth::{AuthenticateRequest, ConfigureComponents};
use crate::extract::RedfishAuth;
use axum::{
    extract::{Path, State},
    http::StatusCode,
    response::IntoResponse,
    routing, Json,
};
use redfish_codegen::{
    api::v1::computer_system_detail::reset::{self, Reset},
    models::computer_system::v1_20_0::ResetRequestBody,
};

pub struct ResetRouter(routing::MethodRouter);

impl ResetRouter {
    pub fn new<S>(state: S) -> Self
    where
        S: AsRef<dyn AuthenticateRequest> + Reset + Send + Sync + Clone + 'static,
    {
        let router = routing::post(
            |State(mut state): State<S>,
             Path(id): Path<String>,
             _: RedfishAuth<ConfigureComponents>,
             Json(body): Json<ResetRequestBody>| async move {
                match state.post(String::default(), id, body) {
                    reset::ResetPostResponse::Ok(error) => {
                        (StatusCode::OK, Json(error)).into_response()
                    }
                    reset::ResetPostResponse::Created(error) => {
                        (StatusCode::CREATED, Json(error)).into_response()
                    }
                    reset::ResetPostResponse::Accepted(error) => {
                        (StatusCode::ACCEPTED, Json(error)).into_response()
                    }
                    reset::ResetPostResponse::NoContent => StatusCode::NO_CONTENT.into_response(),
                    reset::ResetPostResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .with_state(state);

        ResetRouter(router)
    }
}

impl Into<routing::MethodRouter> for ResetRouter {
    fn into(self) -> routing::MethodRouter {
        self.0
    }
}
