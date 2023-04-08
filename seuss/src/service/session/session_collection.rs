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

use redfish_codegen::{
    api::v1::session_service::sessions,
    models::{
        odata_v4, resource, session::v1_6_0,
        session_collection::SessionCollection as SessionCollectionModel,
    },
};

use crate::auth::{AuthenticateRequest, SessionAuthentication, SessionManagement};

#[derive(Clone)]
pub struct InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    auth_handler: S,
}

impl<S> InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    pub fn new(auth_handler: S) -> Self {
        Self { auth_handler }
    }
}

impl<S> SessionManagement for InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    fn session_is_valid(
        &self,
        token: String,
        origin: Option<String>,
    ) -> Result<crate::auth::AuthenticatedUser, redfish_codegen::models::redfish::Error> {
        todo!()
    }
}

pub enum SessionRequest {
    Create {
        username: String,
        password: String,
        origin: Option<String>,
    },
    Destroy {
        id: String,
    },
    Validate {
        id: String,
        origin: Option<String>,
    },
}

pub enum SessionResponse {
    Created(String),
    Destroyed,
    IsValid(bool),
}

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
    S: Clone + SessionManagement,
    A: Clone + AuthenticateRequest + 'static,
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
}

impl<S, A> sessions::Sessions for SessionCollection<S, A>
where
    S: Clone + SessionManagement,
    A: Clone + AuthenticateRequest + 'static,
{
    fn get(&self) -> sessions::SessionsGetResponse {
        sessions::SessionsGetResponse::Ok(SessionCollectionModel {
            name: self.name.clone(),
            odata_id: self.odata_id.clone(),
            ..Default::default()
        })
    }

    fn post(&mut self, session: v1_6_0::Session) -> sessions::SessionsPostResponse {
        todo!()
    }
}
