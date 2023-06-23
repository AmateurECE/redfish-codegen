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

use axum::{extract::State, http::StatusCode, Json, Router};
use redfish_codegen::models::{
    odata_v4, resource, session::v1_6_0::Session,
    session_collection::SessionCollection as SessionCollectionModel,
};
use redfish_core::auth::AuthenticateRequest;

use crate::auth::SessionManagement;

#[derive(Clone)]
pub struct SessionCollection<S, A>
where
    A: Clone + AuthenticateRequest + 'static,
    S: Clone + SessionManagement,
{
    odata_id: odata_v4::Id,
    name: resource::Name,
    auth_handler: A,
    session_manager: S,
}

impl<S, A> AsRef<dyn AuthenticateRequest> for SessionCollection<S, A>
where
    S: Clone + SessionManagement,
    A: Clone + AuthenticateRequest + 'static,
{
    fn as_ref(&self) -> &(dyn AuthenticateRequest + 'static) {
        &self.auth_handler
    }
}

impl<S, A> SessionCollection<S, A>
where
    S: Clone + SessionManagement + Send + Sync + 'static,
    A: Clone + AuthenticateRequest + Send + Sync + 'static,
{
    pub fn new(
        odata_id: odata_v4::Id,
        name: resource::Name,
        auth_handler: A,
        session_manager: S,
    ) -> Self {
        Self {
            odata_id,
            name,
            auth_handler,
            session_manager,
        }
    }

    pub fn into_router(self) -> Router {
        redfish_axum::session_collection::SessionCollection::default()
            .get(|State(state): State<Self>| async move {
                match state.session_manager.sessions() {
                    Ok(members) => Ok(Json(SessionCollectionModel {
                        name: state.name.clone(),
                        odata_id: state.odata_id.clone(),
                        members_odata_count: odata_v4::Count(members.len().try_into().unwrap()),
                        members,
                        ..Default::default()
                    })),
                    Err(error) => Err(Json(error)),
                }
            })
            .post(
                |State(mut state): State<Self>, Json(session): Json<Session>| async move {
                    match state.session_manager.create_session(session) {
                        Ok(session) => Ok((StatusCode::CREATED, Json(session))),
                        Err(error) => Err(Json(error)),
                    }
                },
            )
            .into_router()
            .with_state(self)
    }
}
