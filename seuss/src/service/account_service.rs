use std::str::FromStr;

use axum::{extract::OriginalUri, response::Response, Extension, Router};
use redfish_axum::{role::Role, role_collection::RoleCollection};
use redfish_codegen::models::{
    account_service::v1_12_0::AccountService as AccountServiceModel, odata_v4,
    privileges::PrivilegeType, resource, role::v1_3_1::Role as RoleModel,
    role_collection::RoleCollection as RoleCollectionModel,
};
use redfish_core::{auth::AuthenticateRequest, privilege};
use strum::IntoEnumIterator;

use crate::middleware::ResourceLocator;

pub struct AccountService;

impl AccountService {
    pub fn new() -> Self {
        Self
    }

    pub fn into_router<S>(self) -> Router<S>
    where
        S: AsRef<dyn AuthenticateRequest> + Clone + Send + Sync + 'static,
    {
        redfish_axum::account_service::AccountService::default()
        .get(|OriginalUri(uri): OriginalUri| async move {
            crate::Response(AccountServiceModel {
                odata_id: odata_v4::Id(uri.path().to_string()),
                id: resource::Id("AccountService".to_string()),
                name: resource::Name("Account Service".to_string()),
                roles: Some(odata_v4::IdRef {
                    odata_id: Some(odata_v4::Id(uri.path().to_string() + "/Roles")),
                }),
                ..Default::default()
            })
        })
        .roles(
            RoleCollection::default()
                .get(|OriginalUri(uri): OriginalUri| async move {
                    crate::Response(RoleCollectionModel {
                        odata_id: odata_v4::Id(uri.path().to_string()),
                        name: resource::Name("Roles".to_string()),
                        members: privilege::Role::iter()
                            .map(|role| odata_v4::IdRef {
                                odata_id: Some(odata_v4::Id(uri.path().to_string() + "/" + &role.to_string())),
                            })
                            .collect::<Vec<_>>(),
                        members_odata_count: odata_v4::Count(privilege::Role::iter().count().try_into().unwrap()),
                        ..Default::default()
                    })
                })
                .role(
                    Role::default()
                        .get(|OriginalUri(uri): OriginalUri, Extension(role): Extension<privilege::Role>| async move {
                            crate::Response(RoleModel {
                                odata_id: odata_v4::Id(uri.path().to_string()),
                                id: resource::Id(role.to_string()),
                                name: resource::Name(role.to_string() + " User Role"),
                                assigned_privileges: Some(
                                    role
                                        .privileges()
                                        .into_iter()
                                        .map(PrivilegeType::from)
                                        .collect::<Vec<_>>()
                                ),
                                ..Default::default()
                            })
                        })
                        .into_router()
                        .route_layer(ResourceLocator::new("role_id".to_string(), |id: String| async move {
                            Ok::<_, Response>(privilege::Role::from_str(&id).unwrap())
                        }))
                )
                .into_router()
        )
        .into_router()
    }
}
