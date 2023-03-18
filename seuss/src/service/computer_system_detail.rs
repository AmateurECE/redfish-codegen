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
    extract::{Path, State},
    http::StatusCode,
    response::IntoResponse,
    routing, Json,
};
use redfish_codegen::{
    api::v1::computer_system_detail, models::computer_system::v1_20_0::ComputerSystem,
};

pub mod reset;

pub struct ComputerSystemDetail(routing::MethodRouter);

impl ComputerSystemDetail {
    pub fn new<S>(state: S) -> Self
    where
        S: computer_system_detail::ComputerSystemDetail + Send + Sync + Clone + 'static,
    {
        let router = routing::get(
            |State(state): State<S>, Path(id): Path<String>| async move {
                match state.get(id) {
                    computer_system_detail::ComputerSystemDetailGetResponse::Ok(response) => {
                        Ok(Json(response))
                    }
                    computer_system_detail::ComputerSystemDetailGetResponse::Default(error) => {
                        Err(Json(error))
                    }
                }
            },
        )
        .put(
            |State(mut state): State<S>,
             Path(id): Path<String>,
             Json(body): Json<ComputerSystem>| async move {
                match state.put(id, body) {
                    computer_system_detail::ComputerSystemDetailPutResponse::Ok(
                        computer_system,
                    ) => (StatusCode::OK, Json(computer_system)).into_response(),
                    computer_system_detail::ComputerSystemDetailPutResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    computer_system_detail::ComputerSystemDetailPutResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    computer_system_detail::ComputerSystemDetailPutResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .delete(
            |State(mut state): State<S>, Path(id): Path<String>| async move {
                match state.delete(id) {
                    computer_system_detail::ComputerSystemDetailDeleteResponse::Ok(
                        computer_system,
                    ) => (StatusCode::OK, Json(computer_system)).into_response(),
                    computer_system_detail::ComputerSystemDetailDeleteResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    computer_system_detail::ComputerSystemDetailDeleteResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    computer_system_detail::ComputerSystemDetailDeleteResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .patch(
            |State(mut state): State<S>,
             Path(id): Path<String>,
             Json(body): Json<serde_json::Value>| async move {
                match state.patch(id, body) {
                    computer_system_detail::ComputerSystemDetailPatchResponse::Ok(
                        computer_system,
                    ) => (StatusCode::OK, Json(computer_system)).into_response(),
                    computer_system_detail::ComputerSystemDetailPatchResponse::Accepted(task) => {
                        (StatusCode::ACCEPTED, Json(task)).into_response()
                    }
                    computer_system_detail::ComputerSystemDetailPatchResponse::NoContent => {
                        StatusCode::NO_CONTENT.into_response()
                    }
                    computer_system_detail::ComputerSystemDetailPatchResponse::Default(error) => {
                        (StatusCode::BAD_REQUEST, Json(error)).into_response()
                    }
                }
            },
        )
        .with_state(state);

        ComputerSystemDetail(router)
    }
}

impl Into<routing::MethodRouter> for ComputerSystemDetail {
    fn into(self) -> routing::MethodRouter {
        self.0
    }
}
