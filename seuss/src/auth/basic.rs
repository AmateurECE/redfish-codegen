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

use crate::auth::{AuthenticateRequest, AuthenticatedUser, Role};
use axum::http::request::Parts;
use redfish_codegen::models::redfish;

pub trait BasicAuthentication {
    fn authenticate(username: &str, password: &str) -> Result<(), String>;
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
    fn authenticate_request(
        &self,
        _parts: &mut Parts,
    ) -> Result<AuthenticatedUser, redfish::Error> {
        Ok(AuthenticatedUser {
            username: String::default(),
            role: Role::ReadOnly,
        })
    }
}
