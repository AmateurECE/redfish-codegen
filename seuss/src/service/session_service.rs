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
    http::StatusCode,
    response::IntoResponse,
    routing::{self, MethodRouter},
    Json,
};
use redfish_codegen::{api::v1::session_service, models::session_service::v1_1_8};

use crate::{
    auth::{AuthenticateRequest, ConfigureManager, Login},
    extract::RedfishAuth,
};

pub mod sessions;

pub struct SessionService(MethodRouter);

impl SessionService {
    pub fn new<S>(state: S) -> Self
    where
        S: AsRef<dyn AuthenticateRequest>
            + session_service::SessionService
            + Send
            + Sync
            + Clone
            + 'static,
    {
        let router = routing::get(|State(state): State<S>, _: RedfishAuth<Login>| async move {
            match state.get() {
                session_service::SessionServiceGetResponse::Ok(response) => Ok(Json(response)),
                session_service::SessionServiceGetResponse::Default(response) => {
                    Err(Json(response))
                }
            }
        })
        .put(
            |State(mut state): State<S>,
             _: RedfishAuth<ConfigureManager>,
             Json(body): Json<v1_1_8::SessionService>| async move {
                match state.put(body) {
                    session_service::SessionServicePutResponse::Ok(response) => {
                        (StatusCode::OK, Json(response)).into_response()
                    }
                    session_service::SessionServicePutResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    session_service::SessionServicePutResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    session_service::SessionServicePutResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .patch(
            |State(mut state): State<S>,
             _: RedfishAuth<ConfigureManager>,
             Json(body): Json<serde_json::Value>| async move {
                match state.patch(body) {
                    session_service::SessionServicePatchResponse::Ok(response) => {
                        (StatusCode::OK, Json(response)).into_response()
                    }
                    session_service::SessionServicePatchResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    session_service::SessionServicePatchResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    session_service::SessionServicePatchResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .with_state(state);
        Self(router)
    }
}

impl Into<MethodRouter> for SessionService {
    fn into(self) -> MethodRouter {
        self.0
    }
}
