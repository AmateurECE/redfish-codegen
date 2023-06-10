use axum::{
    http::{request::Parts, StatusCode},
    response::{AppendHeaders, IntoResponse, Response},
    Json,
};
use redfish_codegen::{models::redfish, registries::base::v1_15_0::Base};

use crate::error;
use crate::privilege::Role;

#[derive(Clone)]
pub struct AuthenticatedUser {
    pub username: String,
    pub role: Role,
}

pub fn unauthorized_with_error(error: redfish::Error, challenge: &[&str]) -> Response {
    (
        StatusCode::UNAUTHORIZED,
        AppendHeaders([("WWW-Authenticate", challenge.join(", "))]),
        Json(error),
    )
        .into_response()
}

pub fn unauthorized(challenge: &[&str]) -> Response {
    unauthorized_with_error(
        error::one_message(Base::InsufficientPrivilege.into()),
        challenge,
    )
}

pub fn insufficient_privilege() -> redfish::Error {
    error::one_message(Base::InsufficientPrivilege.into())
}

pub trait AuthenticateRequest {
    fn authenticate_request(
        &self,
        parts: &mut Parts,
    ) -> Result<Option<AuthenticatedUser>, Response>;
    fn challenge(&self) -> Vec<&'static str>;
}
