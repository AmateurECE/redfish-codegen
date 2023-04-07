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

use axum::{http::request::Parts, response::Response};
use base64::engine::{general_purpose, Engine};
use redfish_codegen::models::redfish;
use std::str;

use super::unauthorized;
use super::{AuthenticateRequest, AuthenticatedUser};

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
            .ok_or_else(|| unauthorized(&self.challenge()))?
            .to_str()
            .map_err(|_| unauthorized(&self.challenge()))?
            .strip_prefix("Basic ")
            .ok_or_else(|| unauthorized(&self.challenge()))?
            .to_string();

        let result = general_purpose::STANDARD
            .decode(&authorization)
            .map_err(|_| unauthorized(&self.challenge()))?;

        let credentials: Vec<&str> = str::from_utf8(&result)
            .map_err(|_| unauthorized(&self.challenge()))?
            .split(":")
            .collect();

        if credentials.len() != 2 {
            return Err(unauthorized(&self.challenge()));
        }

        self.authenticator
            .authenticate(credentials[0].to_string(), credentials[1].to_string())
            .map_err(|_| unauthorized(&self.challenge()))
    }

    fn challenge(&self) -> Vec<&'static str> {
        vec!["Basic"]
    }
}
