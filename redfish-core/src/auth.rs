use axum::{
    http::{request::Parts, StatusCode},
    response::{AppendHeaders, IntoResponse, Response},
    Json,
};
use redfish_codegen::{models::redfish, registries::base::v1_16_0::Base};

use crate::privilege::Role;
use crate::{convert::IntoRedfishMessage, error};

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
        error::one_message(Base::InsufficientPrivilege.into_redfish_message()),
        challenge,
    )
}

pub fn insufficient_privilege() -> redfish::Error {
    error::one_message(Base::InsufficientPrivilege.into_redfish_message())
}

pub trait AuthenticateRequest {
    fn authenticate_request(
        &self,
        parts: &mut Parts,
    ) -> Result<Option<AuthenticatedUser>, Response>;
    fn challenge(&self) -> Vec<&'static str>;
}
