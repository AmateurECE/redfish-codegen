use redfish_core::auth::{AuthenticateRequest, AuthenticatedUser};

use super::{
    BasicAuthentication, BasicAuthenticationProxy, SessionAuthenticationProxy, SessionManagement,
};

#[derive(Clone)]
pub struct CombinedAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone,
    S: SessionManagement + Clone,
{
    basic: BasicAuthenticationProxy<B>,
    session: SessionAuthenticationProxy<S>,
}

impl<'a, B, S> AsRef<dyn AuthenticateRequest + 'a> for CombinedAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone + 'a,
    S: SessionManagement + Clone + 'a,
{
    fn as_ref(&self) -> &(dyn AuthenticateRequest + 'a) {
        self
    }
}

impl<B, S> CombinedAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone,
    S: SessionManagement + Clone,
{
    pub fn new(session: S, basic: B) -> Self {
        Self {
            basic: BasicAuthenticationProxy::new(basic),
            session: SessionAuthenticationProxy::new(session),
        }
    }
}

impl<B, S> AuthenticateRequest for CombinedAuthenticationProxy<B, S>
where
    B: BasicAuthentication + Clone,
    S: SessionManagement + Clone,
{
    fn authenticate_request(
        &self,
        parts: &mut axum::http::request::Parts,
    ) -> Result<Option<AuthenticatedUser>, axum::response::Response> {
        // Try session authentication first, then basic authentication
        self.basic
            .authenticate_request(parts)
            .or_else(|_| self.session.authenticate_request(parts))
    }

    fn challenge(&self) -> Vec<&'static str> {
        vec!["Session", "Basic"]
    }
}
