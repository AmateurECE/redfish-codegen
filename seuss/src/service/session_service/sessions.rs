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
use redfish_codegen::{api::v1, models::session::v1_6_0};

use crate::{
    auth::{AuthenticateRequest, Login},
    extract::RedfishAuth,
};

pub struct Sessions(MethodRouter);

impl Sessions {
    pub fn new<S>(state: S) -> Self
    where
        S: AsRef<dyn AuthenticateRequest>
            + v1::session_service::sessions::Sessions
            + Send
            + Sync
            + Clone
            + 'static,
    {
        let router = routing::get(|State(state): State<S>, _: RedfishAuth<Login>| async move {
            match state.get() {
                v1::session_service::sessions::SessionsGetResponse::Ok(response) => {
                    Ok(Json(response))
                }
                v1::session_service::sessions::SessionsGetResponse::Default(error) => {
                    Err(Json(error))
                }
            }
        })
        .post(
            |State(mut state): State<S>,
             _: RedfishAuth<Login>,
             Json(body): Json<v1_6_0::Session>| async move {
                match state.post(body) {
                    v1::session_service::sessions::SessionsPostResponse::Created(session) => {
                        (StatusCode::CREATED, Json(session)).into_response()
                    }
                    v1::session_service::sessions::SessionsPostResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    v1::session_service::sessions::SessionsPostResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    v1::session_service::sessions::SessionsPostResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .with_state(state);

        Self(router)
    }
}

impl Into<MethodRouter> for Sessions {
    fn into(self) -> MethodRouter {
        self.0
    }
}
