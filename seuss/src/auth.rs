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

use axum::http::request::Parts;
use redfish_codegen::models::redfish;
use std::fmt;

pub trait Privilege: fmt::Display {}

pub struct Login;
impl fmt::Display for Login {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Login")
    }
}
impl Privilege for Login {}

pub struct ConfigureManager;
impl fmt::Display for ConfigureManager {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "ConfigureManager")
    }
}
impl Privilege for ConfigureManager {}

pub struct ConfigureUsers;
impl fmt::Display for ConfigureUsers {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "ConfigureUsers")
    }
}
impl Privilege for ConfigureUsers {}

pub struct ConfigureComponents;
impl fmt::Display for ConfigureComponents {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "ConfigureComponents")
    }
}
impl Privilege for ConfigureComponents {}

pub struct ConfigureSelf;
impl fmt::Display for ConfigureSelf {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "ConfigureSelf")
    }
}
impl Privilege for ConfigureSelf {}

pub enum Role {
    Administrator,
    Operator,
    ReadOnly,
}

impl Role {
    pub fn privileges(&self) -> Vec<String> {
        match &self {
            Self::Administrator => vec![
                "Login".to_string(),
                "ConfigureManager".to_string(),
                "ConfigureUsers".to_string(),
                "ConfigureComponents".to_string(),
                "ConfigureSelf".to_string(),
            ],
            Self::Operator => vec![
                "Login".to_string(),
                "ConfigureComponents".to_string(),
                "ConfigureSelf".to_string(),
            ],
            Self::ReadOnly => vec!["Login".to_string(), "ConfigureSelf".to_string()],
        }
    }
}

pub struct AuthenticatedUser {
    pub username: String,
    pub role: Role,
}

pub trait AuthenticateRequest {
    fn authenticate_request(&self, parts: &mut Parts) -> Result<AuthenticatedUser, redfish::Error>;
}

pub trait BasicAuthentication {
    fn authenticate(username: &str, password: &str) -> Result<(), String>;
}

#[derive(Clone)]
pub struct BasicAuthenticationProxy<B>
where
    B: BasicAuthentication + Clone,
{
    pub authenticator: B,
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

// pub trait SessionAuthentication {
//     fn session_start(username: &str, password: &str) -> Result<(), String>;
// }

// #[derive(Clone)]
// pub struct SessionAuthenticationProxy<B, S>
// where
//     B: BasicAuthentication + Clone,
//     S: SessionAuthentication + Clone,
// {
//     basic: BasicAuthenticationProxy<B>,
//     authenticator: S,
// }
