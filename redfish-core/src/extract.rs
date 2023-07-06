use crate::{
    auth::{unauthorized, AuthenticateRequest, AuthenticatedUser},
    privilege::SatisfiesPrivilege,
};
use axum::{async_trait, extract::FromRequestParts, http::request::Parts, response::Response};
use std::marker::PhantomData;

pub struct RedfishAuth<T: SatisfiesPrivilege> {
    pub user: Option<AuthenticatedUser>,
    privilege: PhantomData<T>,
}

#[async_trait]
impl<S, T> FromRequestParts<S> for RedfishAuth<T>
where
    S: AsRef<dyn AuthenticateRequest> + Send + Sync + Clone + 'static,
    T: SatisfiesPrivilege,
{
    type Rejection = Response;

    async fn from_request_parts(parts: &mut Parts, state: &S) -> Result<Self, Self::Rejection> {
        match state.as_ref().authenticate_request(parts) {
            Ok(Some(user)) => {
                if T::is_satisfied(&user.role.privileges()) {
                    Ok(RedfishAuth::<T> {
                        user: Some(user),
                        privilege: Default::default(),
                    })
                } else {
                    Err(unauthorized(&state.as_ref().challenge()))
                }
            }
            Ok(None) => Ok(RedfishAuth::<T> {
                user: None,
                privilege: Default::default(),
            }),
            Err(error) => Err(error),
        }
    }
}
