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

use crate::auth::Privilege;
use crate::redfish_error;
use axum::{
    async_trait,
    extract::FromRequestParts,
    http::{request::Parts, StatusCode},
    Json,
};
use redfish_codegen::{models::redfish, registries::base::v1_15_0::Base};
use std::marker::PhantomData;

pub struct RedfishAuth<T: Privilege> {
    privilege: PhantomData<T>,
}

#[async_trait]
impl<S, T> FromRequestParts<S> for RedfishAuth<T>
where
    S: Send + Sync,
    T: Privilege,
{
    type Rejection = (StatusCode, Json<redfish::Error>);

    async fn from_request_parts(parts: &mut Parts, state: &S) -> Result<Self, Self::Rejection> {
        Err((
            StatusCode::UNAUTHORIZED,
            Json(redfish_error::one_message(
                Base::ResourceAtUriUnauthorized(parts.uri.to_string(), "Unauthorized".to_string())
                    .into(),
            )),
        ))
    }
}
