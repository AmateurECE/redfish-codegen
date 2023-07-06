use core::fmt;
use redfish_codegen::models::redfish;
use redfish_core::{
    auth::{insufficient_privilege, AuthenticatedUser},
    privilege::Role,
};
use std::{collections::HashMap, error, ffi::OsStr};
use users::Group;

use super::{BasicAuthentication, SessionAuthentication};

const DEFAULT_PAM_SERVICE: &str = "redfish";

#[derive(Debug)]
pub struct MissingGroupError(String);
impl fmt::Display for MissingGroupError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Group {} does not exist", &self.0)
    }
}

impl error::Error for MissingGroupError {
    fn source(&self) -> Option<&(dyn error::Error + 'static)> {
        None
    }
}

#[derive(Clone)]
pub struct LinuxPamAuthenticator {
    service: String,
    role_map: HashMap<Role, Group>,
}

impl LinuxPamAuthenticator {
    pub fn new(group_names: HashMap<Role, String>) -> Result<Self, MissingGroupError> {
        let role_map = group_names
            .iter()
            .map(|(role, name)| match users::get_group_by_name(&name) {
                Some(group) => Ok((*role, group)),
                None => Err(MissingGroupError(name.clone())),
            })
            .collect::<Result<HashMap<Role, Group>, MissingGroupError>>()?;
        Ok(LinuxPamAuthenticator {
            service: DEFAULT_PAM_SERVICE.to_string(),
            role_map,
        })
    }

    fn get_user_role(&self, username: &str) -> Result<Role, redfish::Error> {
        let gid = users::get_user_by_name(username)
            .ok_or_else(insufficient_privilege)?
            .primary_group_id();
        let groups = users::get_user_groups(&username, gid).ok_or_else(insufficient_privilege)?;
        let group_names = groups
            .iter()
            .map(|group| group.name())
            .collect::<Vec<&OsStr>>();

        let (role, _) = self
            .role_map
            .iter()
            .find(|(_, group)| group_names.contains(&group.name()))
            .ok_or_else(insufficient_privilege)?;

        Ok(*role)
    }
}

impl BasicAuthentication for LinuxPamAuthenticator {
    fn authenticate(
        &self,
        username: String,
        password: String,
    ) -> Result<AuthenticatedUser, redfish::Error> {
        let mut auth = pam::Authenticator::with_password(&self.service).unwrap();
        auth.get_handler().set_credentials(&username, &password);
        if auth.authenticate().is_err() {
            return Err(insufficient_privilege());
        }

        let role = self.get_user_role(&username)?;
        Ok(AuthenticatedUser { username, role })
    }
}

impl SessionAuthentication for LinuxPamAuthenticator {
    fn open_session(
        &self,
        username: String,
        password: String,
    ) -> Result<AuthenticatedUser, redfish::Error> {
        let mut auth = pam::Authenticator::with_password(&self.service).unwrap();
        auth.get_handler().set_credentials(&username, &password);
        // As opposed to the basic handler, we also invoke a PAM session
        if auth.authenticate().is_err() || auth.open_session().is_err() {
            return Err(insufficient_privilege());
        }

        let role = self.get_user_role(&username)?;
        Ok(AuthenticatedUser { username, role })
    }

    fn close_session(&self) -> Result<(), redfish::Error> {
        todo!()
    }
}
