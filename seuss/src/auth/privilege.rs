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

#[derive(PartialEq)]
pub enum Privilege {
    Login,
    ConfigureComponents,
    ConfigureManager,
    ConfigureSelf,
    ConfigureUsers,
}

pub trait AsPrivilege {
    fn privilege() -> Privilege;
}

pub struct Login;
impl AsPrivilege for Login {
    fn privilege() -> Privilege {
        Privilege::Login
    }
}

pub struct ConfigureManager;
impl AsPrivilege for ConfigureManager {
    fn privilege() -> Privilege {
        Privilege::ConfigureManager
    }
}

pub struct ConfigureUsers;
impl AsPrivilege for ConfigureUsers {
    fn privilege() -> Privilege {
        Privilege::ConfigureUsers
    }
}

pub struct ConfigureComponents;
impl AsPrivilege for ConfigureComponents {
    fn privilege() -> Privilege {
        Privilege::ConfigureComponents
    }
}

pub struct ConfigureSelf;
impl AsPrivilege for ConfigureSelf {
    fn privilege() -> Privilege {
        Privilege::ConfigureSelf
    }
}

#[derive(PartialEq)]
pub enum Role {
    Administrator,
    Operator,
    ReadOnly,
}

impl Role {
    pub fn privileges(&self) -> Vec<Privilege> {
        match &self {
            Self::Administrator => vec![
                Privilege::Login,
                Privilege::ConfigureManager,
                Privilege::ConfigureUsers,
                Privilege::ConfigureComponents,
                Privilege::ConfigureSelf,
            ],
            Self::Operator => vec![
                Privilege::Login,
                Privilege::ConfigureComponents,
                Privilege::ConfigureSelf,
            ],
            Self::ReadOnly => vec![Privilege::Login, Privilege::ConfigureSelf],
        }
    }
}
