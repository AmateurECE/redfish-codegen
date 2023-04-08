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

mod session;
pub use session::*;

mod basic;
pub use basic::*;

mod privilege;
pub use privilege::*;

mod combination;
pub use combination::*;

#[cfg(feature = "auth-pam")]
pub mod pam;

use axum::{
    http::{request::Parts, StatusCode},
    response::{AppendHeaders, IntoResponse, Response},
    Json,
};
use redfish_codegen::{models::redfish, registries::base::v1_15_0::Base};

use crate::redfish_error;

pub struct AuthenticatedUser {
    pub username: String,
    pub role: Role,
}

pub fn unauthorized(realm: &[&str]) -> Response {
    (
        StatusCode::UNAUTHORIZED,
        AppendHeaders([("WWW-Authenticate", realm.join(", "))]),
        Json(redfish_error::one_message(
            Base::InsufficientPrivilege.into(),
        )),
    )
        .into_response()
}

pub fn insufficient_privilege() -> redfish::Error {
    redfish_error::one_message(Base::InsufficientPrivilege.into())
}

pub trait AuthenticateRequest {
    fn authenticate_request(&self, parts: &mut Parts) -> Result<AuthenticatedUser, Response>;
    fn challenge(&self) -> Vec<&'static str>;
}
