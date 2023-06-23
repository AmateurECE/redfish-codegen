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

use axum::{extract::State, Json, Router};
use redfish_codegen::models::{odata_v4, resource, session_service::v1_1_8};
use redfish_core::auth::AuthenticateRequest;

#[derive(Clone)]
pub struct SessionService<S>
where
    S: Clone + AuthenticateRequest,
{
    id: resource::Id,
    name: resource::Name,
    odata_id: odata_v4::Id,
    sessions: odata_v4::Id,
    auth_handler: S,
}

impl<S> AsRef<dyn AuthenticateRequest> for SessionService<S>
where
    S: AuthenticateRequest + Clone + 'static,
{
    fn as_ref(&self) -> &(dyn AuthenticateRequest + 'static) {
        &self.auth_handler
    }
}

impl<S> SessionService<S>
where
    S: AuthenticateRequest + Clone + Send + Sync + 'static,
{
    pub fn new(
        odata_id: odata_v4::Id,
        name: resource::Name,
        sessions: odata_v4::Id,
        auth_handler: S,
    ) -> Self {
        SessionService {
            id: resource::Id("sessions".to_string()),
            name,
            odata_id,
            sessions,
            auth_handler,
        }
    }

    pub fn into_router(self) -> Router {
        redfish_axum::session_service::SessionService::default()
            .get(|State(state): State<Self>| async move {
                Json(v1_1_8::SessionService {
                    id: state.id.clone(),
                    name: state.name.clone(),
                    odata_id: state.odata_id.clone(),
                    sessions: Some(odata_v4::IdRef {
                        odata_id: Some(state.sessions.clone()),
                    }),
                    ..Default::default()
                })
            })
            .into_router()
            .with_state(self)
    }
}
