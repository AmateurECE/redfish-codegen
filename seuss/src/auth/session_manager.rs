use chrono::{DateTime, Local};
use redfish_codegen::{
    models::{odata_v4, redfish, resource, session::v1_6_0},
    registries::base::v1_15_0::Base,
};
use redfish_core::{auth::AuthenticatedUser, error};
use std::{
    collections::{hash_map::DefaultHasher, HashMap},
    hash::Hasher,
    sync::{Arc, Mutex},
    time::Duration,
};

use crate::auth::{SessionAuthentication, SessionManagement};

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
    last_id: Arc<Mutex<u64>>,
    timeout: Duration,
    auth_handler: S,
}

impl<S> InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    /// Default duration is 30 minutes.
    const DEFAULT_DURATION: Duration = Duration::from_secs(1800);

    pub fn new(auth_handler: S) -> Self {
        Self::with_duration(Self::DEFAULT_DURATION, auth_handler)
    }

    pub fn with_duration(timeout: Duration, auth_handler: S) -> Self {
        Self {
            auth_handler,
            last_id: Arc::new(Mutex::new(0)),
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
        let value = noise.to_string() + "|" + username;
        hasher.write(value.as_bytes());
        hasher.finish().to_string()
    }
}

impl<S> SessionManagement for InMemorySessionManager<S>
where
    S: SessionAuthentication + Clone,
{
    type Id = usize;
    fn session_is_valid(
        &self,
        token: String,
        origin: Option<String>,
    ) -> Result<AuthenticatedUser, redfish_codegen::models::redfish::Error> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions
            .get(&token)
            .ok_or_else(|| error::one_message(Base::NoValidSession.into()))?;
        if self.session_is_active()(&session) && session.session.client_origin_ip_address == origin
        {
            Ok(session.user.clone())
        } else {
            Err(error::one_message(Base::NoValidSession.into()))
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
        base_path: String,
    ) -> Result<v1_6_0::Session, redfish::Error> {
        let user_name = session.user_name.clone().ok_or_else(|| {
            error::one_message(Base::PropertyMissing("UserName".to_string()).into())
        })?;
        let password = session.password.ok_or_else(|| {
            error::one_message(Base::PropertyMissing("Password".to_string()).into())
        })?;
        let user = self
            .auth_handler
            .open_session(user_name.clone(), password)?;

        let mut sessions = self.sessions.lock().unwrap();
        let mut last_id = self.last_id.lock().unwrap();
        *last_id += 1;
        let mut id = last_id.to_string();
        while sessions.iter().any(|session| session.1.session.id.0 == id) {
            *last_id += 1;
            id = last_id.to_string();
        }
        let token = self.token(&user_name);

        let created_session = v1_6_0::Session {
            user_name: Some(user_name),
            odata_id: odata_v4::Id(base_path + "/" + &id),
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

    fn delete_session(&mut self, id: Self::Id) -> Result<(), redfish::Error> {
        let mut sessions = self.sessions.lock().unwrap();
        sessions
            .iter()
            .find_map(|pair| {
                if pair.1.session.id.0 == id.to_string() {
                    Some(pair.0.clone())
                } else {
                    None
                }
            })
            .and_then(|id| sessions.remove(&id))
            .map(|_| ())
            .ok_or_else(|| error::one_message(Base::NoValidSession.into()))
    }

    fn get_session(&self, id: Self::Id) -> Result<v1_6_0::Session, redfish::Error> {
        let sessions = self.sessions.lock().unwrap();
        sessions
            .iter()
            .find_map(|pair| {
                if pair.1.session.id.0 == id.to_string() {
                    Some(pair.1.session.clone())
                } else {
                    None
                }
            })
            .ok_or_else(|| error::one_message(Base::NoValidSession.into()))
    }
}
