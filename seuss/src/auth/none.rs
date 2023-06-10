use axum::{http::request::Parts, response::Response};
use redfish_core::auth::{AuthenticateRequest, AuthenticatedUser};

#[derive(Clone, Default)]
pub struct NoAuth;

impl AuthenticateRequest for NoAuth {
    fn authenticate_request(
        &self,
        _parts: &mut Parts,
    ) -> Result<Option<AuthenticatedUser>, Response> {
        Ok(None)
    }

    fn challenge(&self) -> Vec<&'static str> {
        // Should never be called, because authenticate_request always returns Ok
        unimplemented!()
    }
}

impl<'a> AsRef<dyn AuthenticateRequest + 'a> for NoAuth {
    fn as_ref(&self) -> &(dyn AuthenticateRequest + 'a) {
        self
    }
}
