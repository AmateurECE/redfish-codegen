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

use crate::auth::{unauthorized, AsPrivilege, AuthenticateRequest, AuthenticatedUser};
use axum::{async_trait, extract::FromRequestParts, http::request::Parts, response::Response};
use std::marker::PhantomData;

pub struct RedfishAuth<T: AsPrivilege> {
    pub user: Option<AuthenticatedUser>,
    privilege: PhantomData<T>,
}

#[async_trait]
impl<S, T> FromRequestParts<S> for RedfishAuth<T>
where
    S: AsRef<dyn AuthenticateRequest> + Send + Sync + Clone + 'static,
    T: AsPrivilege,
{
    type Rejection = Response;

    async fn from_request_parts(parts: &mut Parts, state: &S) -> Result<Self, Self::Rejection> {
        match state.as_ref().authenticate_request(parts) {
            Ok(Some(user)) => {
                if user.role.privileges().contains(&T::privilege()) {
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
