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

use crate::auth::{AuthenticateRequest, AuthenticatedUser};
use axum::{http::request::Parts, response::Response};
use redfish_codegen::models::redfish;

use super::{unauthorized, unauthorized_with_error};

pub trait SessionManagement {
    fn session_is_valid(
        &self,
        token: String,
        origin: Option<String>,
    ) -> Result<AuthenticatedUser, redfish::Error>;
}

pub trait SessionAuthentication {
    fn open_session(
        &self,
        username: String,
        password: String,
    ) -> Result<AuthenticatedUser, redfish::Error>;
    fn close_session(&self) -> Result<(), redfish::Error>;
}

#[derive(Clone)]
pub struct SessionAuthenticationProxy<S>
where
    S: SessionManagement + Clone,
{
    authenticator: S,
}

impl<S> SessionAuthenticationProxy<S>
where
    S: SessionManagement + Clone,
{
    pub fn new(authenticator: S) -> Self {
        Self { authenticator }
    }
}

impl<S> AuthenticateRequest for SessionAuthenticationProxy<S>
where
    S: SessionManagement + Clone,
{
    fn authenticate_request(&self, parts: &mut Parts) -> Result<AuthenticatedUser, Response> {
        let token = parts
            .headers
            .get("X-Auth-Token")
            .ok_or_else(|| unauthorized(&self.challenge()))?
            .to_str()
            .map_err(|_| unauthorized(&self.challenge()))?
            .to_string();

        let origin = parts
            .headers
            .get("Origin")
            .map(|value| {
                Ok(value
                    .to_str()
                    .map_err(|_| unauthorized(&self.challenge()))?
                    .to_string())
            })
            .transpose()?;

        self.authenticator
            .session_is_valid(token, origin)
            .map_err(|error| unauthorized_with_error(error, &self.challenge()))
    }

    fn challenge(&self) -> Vec<&'static str> {
        vec!["Session"]
    }
}
