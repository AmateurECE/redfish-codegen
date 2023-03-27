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

use crate::{MissingGroupError, DEFAULT_PAM_SERVICE};
use redfish_codegen::{models::redfish, registries::base::v1_15_0::Base};
use seuss::{
    auth::{AuthenticatedUser, BasicAuthentication, Role},
    redfish_error,
};
use std::{collections::HashMap, ffi::OsStr};
use users::Group;

#[derive(Clone)]
pub struct LinuxPamBasicAuthenticator {
    service: String,
    role_map: HashMap<Role, Group>,
}

impl LinuxPamBasicAuthenticator {
    pub fn new(group_names: HashMap<Role, String>) -> Result<Self, MissingGroupError> {
        let role_map = group_names
            .iter()
            .map(|(role, name)| match users::get_group_by_name(&name) {
                Some(group) => Ok((*role, group)),
                None => Err(MissingGroupError(name.clone())),
            })
            .collect::<Result<HashMap<Role, Group>, MissingGroupError>>()?;
        Ok(LinuxPamBasicAuthenticator {
            service: DEFAULT_PAM_SERVICE.to_string(),
            role_map,
        })
    }
}

fn unauthorized() -> redfish::Error {
    redfish_error::one_message(Base::InsufficientPrivilege.into())
}

impl BasicAuthentication for LinuxPamBasicAuthenticator {
    fn authenticate(
        &self,
        username: String,
        password: String,
    ) -> Result<AuthenticatedUser, redfish::Error> {
        let mut auth = pam::Authenticator::with_password(&self.service).unwrap();
        auth.get_handler().set_credentials(&username, &password);
        if auth.authenticate().is_err() || auth.open_session().is_err() {
            return Err(unauthorized());
        }

        let gid = users::get_user_by_name(&username)
            .ok_or_else(|| unauthorized())?
            .primary_group_id();
        let groups = users::get_user_groups(&username, gid).ok_or_else(|| unauthorized())?;
        let group_names = groups
            .iter()
            .map(|group| group.name())
            .collect::<Vec<&OsStr>>();

        let (role, _) = self
            .role_map
            .iter()
            .find(|(_, group)| group_names.contains(&group.name()))
            .ok_or_else(|| unauthorized())?;

        Ok(AuthenticatedUser {
            username: username,
            role: *role,
        })
    }
}
