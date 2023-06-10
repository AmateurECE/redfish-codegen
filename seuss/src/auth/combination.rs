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

use super::{
    AuthenticateRequest, BasicAuthentication, BasicAuthenticationProxy, SessionAuthenticationProxy,
    SessionManagement,
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
    ) -> Result<Option<super::AuthenticatedUser>, axum::response::Response> {
        // Try session authentication first, then basic authentication
        self.basic
            .authenticate_request(parts)
            .or_else(|_| self.session.authenticate_request(parts))
    }

    fn challenge(&self) -> Vec<&'static str> {
        vec!["Session", "Basic"]
    }
}
