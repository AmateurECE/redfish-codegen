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

use crate::auth::{
    AuthenticateRequest, AuthenticatedUser, BasicAuthentication, BasicAuthenticationProxy,
};
use axum::{http::request::Parts, response::Response};

pub trait SessionAuthentication {
    fn session_start(username: &str, password: &str) -> Result<(), String>;
}

#[derive(Clone)]
pub struct SessionAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone,
    S: SessionAuthentication + Clone,
{
    basic: BasicAuthenticationProxy<B>,
    authenticator: S,
}

impl<B, S> SessionAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone,
    S: SessionAuthentication + Clone,
{
    pub fn new(basic: B, authenticator: S) -> Self {
        Self {
            basic: BasicAuthenticationProxy::new(basic),
            authenticator,
        }
    }
}

impl<B, S> AuthenticateRequest for SessionAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone,
    S: SessionAuthentication + Clone,
{
    fn authenticate_request(&self, _parts: &mut Parts) -> Result<AuthenticatedUser, Response> {
        todo!()
    }

    fn unauthorized(&self) -> Response {
        todo!()
    }
}
