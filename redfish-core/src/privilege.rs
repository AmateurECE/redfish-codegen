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

use std::marker::PhantomData;

#[derive(Clone, PartialEq, Eq)]
#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
pub enum Privilege {
    Login,
    ConfigureComponents,
    ConfigureManager,
    ConfigureSelf,
    ConfigureUsers,
}

pub trait SatisfiesPrivilege {
    fn is_satisfied(privileges: &[Privilege]) -> bool;
}

#[derive(Clone, Default)]
pub struct Login;
impl SatisfiesPrivilege for Login {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::Login)
    }
}

#[derive(Clone, Default)]
pub struct ConfigureManager;
impl SatisfiesPrivilege for ConfigureManager {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureManager)
    }
}

#[derive(Clone, Default)]
pub struct ConfigureUsers;
impl SatisfiesPrivilege for ConfigureUsers {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureUsers)
    }
}

#[derive(Clone, Default)]
pub struct ConfigureComponents;
impl SatisfiesPrivilege for ConfigureComponents {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureComponents)
    }
}

#[derive(Clone, Default)]
pub struct ConfigureSelf;
impl SatisfiesPrivilege for ConfigureSelf {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureSelf)
    }
}

#[derive(Clone)]
pub struct And<P, R>(PhantomData<fn() -> (P, R)>)
where
    P: SatisfiesPrivilege + Clone,
    R: SatisfiesPrivilege + Clone;
impl<P, R> SatisfiesPrivilege for And<P, R>
where
    P: SatisfiesPrivilege + Clone,
    R: SatisfiesPrivilege + Clone,
{
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        P::is_satisfied(privileges) && R::is_satisfied(privileges)
    }
}

#[derive(Clone)]
pub struct Or<P, R>(PhantomData<fn() -> (P, R)>)
where
    P: SatisfiesPrivilege + Clone,
    R: SatisfiesPrivilege + Clone;
impl<P, R> SatisfiesPrivilege for Or<P, R>
where
    P: SatisfiesPrivilege + Clone,
    R: SatisfiesPrivilege + Clone,
{
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        P::is_satisfied(privileges) || R::is_satisfied(privileges)
    }
}

#[derive(Clone, Copy, PartialEq, Eq, Hash)]
#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
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

pub trait OperationPrivilegeMapping {
    type Get: SatisfiesPrivilege;
    type Head: SatisfiesPrivilege;
    type Post: SatisfiesPrivilege;
    type Put: SatisfiesPrivilege;
    type Patch: SatisfiesPrivilege;
    type Delete: SatisfiesPrivilege;
}
