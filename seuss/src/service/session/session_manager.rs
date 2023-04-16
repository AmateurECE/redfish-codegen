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

use chrono::{DateTime, Local};
use redfish_codegen::{
    models::{odata_v4, redfish, resource, session::v1_6_0},
    registries::base::v1_15_0::Base,
};
use std::{
    collections::{hash_map::DefaultHasher, HashMap},
    hash::Hasher,
    sync::{Arc, Mutex},
    time::Duration,
};

use crate::{
    auth::{AuthenticatedUser, SessionAuthentication, SessionManagement},
    redfish_error,
};

#[derive(Clone)]
struct ManagedSession {
    session: v1_6_0::Session,
    last_request: DateTime<Local>,
    user: AuthenticatedUser,
}

#[derive(Clone)]
pub struct InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    sessions: Arc<Mutex<HashMap<String, ManagedSession>>>,
    last_id: Arc<Mutex<i64>>,
    collection_odata_id: odata_v4::Id,
    timeout: Duration,
    auth_handler: S,
}

impl<S> InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    /// Default duration is 30 minutes.
    const DEFAULT_DURATION: Duration = Duration::from_secs(1800);

    pub fn new(auth_handler: S, collection_id: odata_v4::Id) -> Self {
        Self::with_duration(Self::DEFAULT_DURATION, auth_handler, collection_id)
    }

    pub fn with_duration(timeout: Duration, auth_handler: S, collection_id: odata_v4::Id) -> Self {
        Self {
            auth_handler,
            last_id: Arc::new(Mutex::new(0)),
            collection_odata_id: collection_id,
            timeout,
            sessions: Arc::new(Mutex::new(HashMap::new())),
        }
    }

    fn session_is_active(&self) -> impl FnMut(&&ManagedSession) -> bool {
        let timeout = self.timeout;
        move |session: &&ManagedSession| {
            let difference: Result<u64, _> = (Local::now() - session.last_request)
                .num_milliseconds()
                .try_into();
            match difference {
                Ok(millis) => timeout > Duration::from_millis(millis),
                Err(_) => false,
            }
        }
    }

    /// Create a session token from the random session ID
    fn token(&self, username: &str) -> String {
        let mut hasher = DefaultHasher::new();
        let noise: u64 = rand::random();
        let value = noise.to_string() + "|" + &username;
        hasher.write(value.as_bytes());
        hasher.finish().to_string()
    }
}

impl<S> SessionManagement for InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    fn session_is_valid(
        &self,
        token: String,
        origin: Option<String>,
    ) -> Result<crate::auth::AuthenticatedUser, redfish_codegen::models::redfish::Error> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions
            .get(&token)
            .ok_or_else(|| redfish_error::one_message(Base::NoValidSession.into()))?;
        if self.session_is_active()(&session) && session.session.client_origin_ip_address == origin
        {
            Ok(session.user.clone())
        } else {
            Err(redfish_error::one_message(Base::NoValidSession.into()))
        }
    }

    fn sessions(&self) -> Result<Vec<odata_v4::IdRef>, redfish::Error> {
        let sessions = self.sessions.lock().unwrap();
        Ok(sessions
            .values()
            .filter(self.session_is_active())
            .map(|session| odata_v4::IdRef {
                odata_id: Some(session.session.odata_id.clone()),
            })
            .collect::<Vec<odata_v4::IdRef>>())
    }

    fn create_session(
        &mut self,
        session: v1_6_0::Session,
    ) -> Result<v1_6_0::Session, redfish::Error> {
        let user_name = session.user_name.clone().ok_or_else(|| {
            redfish_error::one_message(Base::PropertyMissing("UserName".to_string()).into())
        })?;
        let password = session.password.clone().ok_or_else(|| {
            redfish_error::one_message(Base::PropertyMissing("Password".to_string()).into())
        })?;
        let user = self
            .auth_handler
            .open_session(user_name.clone(), password)?;

        let mut sessions = self.sessions.lock().unwrap();
        let mut last_id = self.last_id.lock().unwrap();
        *last_id += 1;
        let id = last_id.to_string();
        let token = self.token(&user_name);

        let created_session = v1_6_0::Session {
            user_name: Some(user_name),
            odata_id: odata_v4::Id(self.collection_odata_id.0.clone() + "/" + &id),
            id: resource::Id(id.clone()),
            name: resource::Name("User Session".to_string()),
            description: Some(resource::Description("User Session".to_string())),
            token: Some(token.clone()),
            ..Default::default()
        };
        let managed_session = ManagedSession {
            session: created_session.clone(),
            user,
            last_request: Local::now(),
        };
        sessions.insert(token, managed_session);
        Ok(created_session)
    }

    fn delete_session(&mut self, token: String) -> Result<(), redfish::Error> {
        let mut sessions = self.sessions.lock().unwrap();
        sessions
            .remove(&token)
            .map(|_| ())
            .ok_or_else(|| redfish_error::one_message(Base::NoValidSession.into()))
    }
}
