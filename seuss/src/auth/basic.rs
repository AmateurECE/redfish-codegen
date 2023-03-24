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
use crate::redfish_error;
use axum::{
    http::{request::Parts, StatusCode},
    response::{AppendHeaders, IntoResponse, Response},
    Json,
};
use base64::engine::{general_purpose, Engine};
use redfish_codegen::{models::redfish, registries::base::v1_15_0::Base};
use std::str;

pub trait BasicAuthentication {
    fn authenticate(
        &self,
        username: String,
        password: String,
    ) -> Result<AuthenticatedUser, redfish::Error>;
}

#[derive(Clone)]
pub struct BasicAuthenticationProxy<B>
where
    B: BasicAuthentication + Clone,
{
    authenticator: B,
}

impl<B> BasicAuthenticationProxy<B>
where
    B: BasicAuthentication + Clone,
{
    pub fn new(authenticator: B) -> Self {
        Self { authenticator }
    }
}

impl<B> AuthenticateRequest for BasicAuthenticationProxy<B>
where
    B: BasicAuthentication + Clone,
{
    fn authenticate_request(&self, parts: &mut Parts) -> Result<AuthenticatedUser, Response> {
        let authorization = parts
            .headers
            .get("Authorization")
            .ok_or_else(|| self.unauthorized())?
            .to_str()
            .map_err(|_| self.unauthorized())?
            .strip_prefix("Basic ")
            .ok_or_else(|| self.unauthorized())?
            .to_string();

        let result = general_purpose::STANDARD
            .decode(&authorization)
            .map_err(|_| self.unauthorized())?;

        let credentials: Vec<&str> = str::from_utf8(&result)
            .map_err(|_| self.unauthorized())?
            .split(":")
            .collect();

        if credentials.len() != 2 {
            return Err(self.unauthorized());
        }

        self.authenticator
            .authenticate(credentials[0].to_string(), credentials[1].to_string())
            .map_err(|_| self.unauthorized())
    }

    fn unauthorized(&self) -> Response {
        (
            StatusCode::UNAUTHORIZED,
            AppendHeaders([("WWW-Authenticate", "Basic")]),
            Json(redfish_error::one_message(
                Base::InsufficientPrivilege.into(),
            )),
        )
            .into_response()
    }
}
