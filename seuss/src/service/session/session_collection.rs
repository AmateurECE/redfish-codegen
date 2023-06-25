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

use std::str::FromStr;

use axum::{
    extract::{OriginalUri, State},
    http::StatusCode,
    response::Response,
    Extension, Json, Router,
};
use redfish_axum::session::Session;
use redfish_codegen::models::{
    odata_v4, resource, session::v1_6_0::Session as SessionModel,
    session_collection::SessionCollection as SessionCollectionModel,
};
use redfish_core::auth::AuthenticateRequest;

use crate::{auth::SessionManagement, middleware::ResourceLocator};

#[derive(Clone)]
pub struct SessionCollection<M, A>
where
    M: Clone + SessionManagement,
    A: Clone + AuthenticateRequest + 'static,
{
    session_manager: M,
    auth_handler: A,
}

impl<M, A> AsRef<dyn AuthenticateRequest> for SessionCollection<M, A>
where
    M: Clone + SessionManagement,
    A: Clone + AuthenticateRequest + 'static,
{
    fn as_ref(&self) -> &(dyn AuthenticateRequest + 'static) {
        &self.auth_handler
    }
}

impl<M, A> SessionCollection<M, A>
where
    M: Clone + SessionManagement + Send + Sync + 'static,
    <M as SessionManagement>::Id: FromStr + Clone + Send + Sync,
    A: Clone + AuthenticateRequest + Send + Sync + 'static,
{
    pub fn new(session_manager: M, auth_handler: A) -> Self {
        Self {
            auth_handler,
            session_manager,
        }
    }

    pub fn into_router<S>(self) -> Router<S> {
        redfish_axum::session_collection::SessionCollection::default()
            .get(
                |OriginalUri(uri): OriginalUri, State(state): State<Self>| async move {
                    match state.session_manager.sessions() {
                        Ok(members) => Ok(Json(SessionCollectionModel {
                            name: resource::Name("SessionCollection".to_string()),
                            odata_id: odata_v4::Id(uri.path().to_string()),
                            members_odata_count: odata_v4::Count(members.len().try_into().unwrap()),
                            members,
                            ..Default::default()
                        })),
                        Err(error) => Err(Json(error)),
                    }
                },
            )
            .post(
                |State(mut state): State<Self>, OriginalUri(uri): OriginalUri, Json(session): Json<SessionModel>| async move {
                    match state.session_manager.create_session(session, uri.path().to_string()) {
                        Ok(session) => Ok((
                            StatusCode::CREATED,
                            [
                                ("X-Auth-Token", session.token.clone().unwrap()),
                                ("Location", session.odata_id.0.clone()),
                            ],
                            Json(session),
                        )),
                        Err(error) => Err(Json(error)),
                    }
                },
            )
            .session(
                Session::default()
                    .get(|Extension(id): Extension<M::Id>, State(state): State<Self>| async move {
                        match state.session_manager.get_session(id) {
                            Ok(session) => Ok(Json(session)),
                            Err(error) => Err(Json(error)),
                        }
                    })
                    .delete(|Extension(id): Extension<M::Id>, State(mut state): State<Self>| async move {
                        match state.session_manager.delete_session(id) {
                            Ok(()) => Ok(StatusCode::NO_CONTENT),
                            Err(error) => Err(Json(error)),
                        }
                    })
                    .into_router()
                    .route_layer(ResourceLocator::new(
                        "session_id".to_string(),
                        |id: M::Id| async move { Ok::<_, Response>(id) }
                    ))
            )
            .into_router()
            .with_state(self)
    }
}
