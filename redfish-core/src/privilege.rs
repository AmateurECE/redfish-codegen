use redfish_codegen::models::privileges::PrivilegeType;
use std::marker::PhantomData;

/// The privileges called out in the Redfish specification.
#[derive(Clone, Copy, PartialEq, Eq, Hash, strum::EnumIter, strum::Display, strum::EnumString)]
#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
pub enum Privilege {
    Login,
    ConfigureComponents,
    ConfigureManager,
    ConfigureSelf,
    ConfigureUsers,
}

impl From<Privilege> for PrivilegeType {
    fn from(value: Privilege) -> Self {
        match value {
            Privilege::Login => PrivilegeType::Login,
            Privilege::ConfigureComponents => PrivilegeType::ConfigureComponents,
            Privilege::ConfigureManager => PrivilegeType::ConfigureManager,
            Privilege::ConfigureSelf => PrivilegeType::ConfigureSelf,
            Privilege::ConfigureUsers => PrivilegeType::ConfigureUsers,
        }
    }
}

pub trait SatisfiesPrivilege {
    fn is_satisfied(privileges: &[Privilege]) -> bool;
}

/// The Login privilege.
#[derive(Clone, Default)]
pub struct Login;
impl SatisfiesPrivilege for Login {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::Login)
    }
}

/// The ConfigureManager privilege.
#[derive(Clone, Default)]
pub struct ConfigureManager;
impl SatisfiesPrivilege for ConfigureManager {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureManager)
    }
}

/// The ConfigureUsers privilege.
#[derive(Clone, Default)]
pub struct ConfigureUsers;
impl SatisfiesPrivilege for ConfigureUsers {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureUsers)
    }
}

/// The ConfigureComponents privilege.
#[derive(Clone, Default)]
pub struct ConfigureComponents;
impl SatisfiesPrivilege for ConfigureComponents {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureComponents)
    }
}

/// The ConfigureSelf privilege.
#[derive(Clone, Default)]
pub struct ConfigureSelf;
impl SatisfiesPrivilege for ConfigureSelf {
    fn is_satisfied(privileges: &[Privilege]) -> bool {
        privileges.contains(&Privilege::ConfigureSelf)
    }
}

/// This struct can be used to disable authentication and authorization for a Redfish service.
#[derive(Clone, Default)]
pub struct NoAuth;
impl SatisfiesPrivilege for NoAuth {
    fn is_satisfied(_privileges: &[Privilege]) -> bool {
        true
    }
}

/// A combinator that requires both privileges to be satisfied.
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

/// A combinator that requires at least one privilege to be satisfied.
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

/// The standard roles defined in the Redfish specification.
#[derive(Clone, Copy, PartialEq, Eq, Hash, strum::EnumIter, strum::Display, strum::EnumString)]
#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
pub enum Role {
    /// Administrators have sufficient privilege to perform any operation.
    Administrator,
    /// Operators have all the privileges of ReadOnly users, plus the ConfigureComponents
    /// privilege.
    Operator,
    /// ReadOnly users have only the Login and ConfigureSelf privileges.
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

/// This trait maps HTTP verbs to required privileges and/or privilege combinators.
pub trait OperationPrivilegeMapping {
    type Get: SatisfiesPrivilege;
    type Head: SatisfiesPrivilege;
    type Post: SatisfiesPrivilege;
    type Put: SatisfiesPrivilege;
    type Patch: SatisfiesPrivilege;
    type Delete: SatisfiesPrivilege;
}
