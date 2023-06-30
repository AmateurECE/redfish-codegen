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

use axum::{
    extract::OriginalUri,
    http::{StatusCode, Uri},
    response::{IntoResponse, Response},
    Extension, Json,
};
use clap::Parser;
use redfish_axum::{
    computer_system::ComputerSystem, computer_system_collection::ComputerSystemCollection,
    odata::OData, service_root::ServiceRoot,
};
use redfish_codegen::{
    models::{
        computer_system::v1_20_0::{
            Actions, ComputerSystem as ComputerSystemModel, Reset, ResetRequestBody,
        },
        computer_system_collection::ComputerSystemCollection as ComputerSystemCollectionModel,
        odata_v4,
        resource::{self, ResetType},
        service_root::v1_15_0::{Links, ServiceRoot as ServiceRootModel},
    },
    registries::base::v1_15_0::Base,
};
use redfish_core::{error, privilege};
use seuss::{
    auth::{pam::LinuxPamAuthenticator, CombinedAuthenticationProxy, InMemorySessionManager},
    middleware::ResourceLocator,
    service::{AccountService, RedfishService, SessionService},
};
use std::{
    collections::HashMap,
    fs::File,
    sync::{Arc, Mutex},
};
use tower_http::trace::TraceLayer;

#[derive(Parser)]
struct Args {
    /// Configuration file
    #[clap(value_parser, short, long)]
    config: String,
}

#[derive(serde::Deserialize)]
#[allow(dead_code)]
struct Configuration {
    #[serde(rename = "role-map")]
    role_map: HashMap<privilege::Role, String>,
    server: seuss::router::Configuration,
}

struct SimpleSystem(resource::PowerState);

async fn service_root(OriginalUri(uri): OriginalUri) -> impl IntoResponse {
    let session_service_path = uri.path().to_string() + "SessionService";
    seuss::Response(ServiceRootModel {
        odata_id: odata_v4::Id(uri.path().to_string()),
        id: resource::Id("simple".to_string()),
        name: resource::Name("Simple Redfish Service".to_string()),
        systems: Some(odata_v4::IdRef {
            odata_id: Some(odata_v4::Id(uri.path().to_string() + "Systems")),
        }),
        session_service: Some(odata_v4::IdRef {
            odata_id: Some(odata_v4::Id(session_service_path.clone())),
        }),
        account_service: Some(odata_v4::IdRef {
            odata_id: Some(odata_v4::Id(uri.path().to_string() + "AccountService")),
        }),
        links: Links {
            sessions: odata_v4::IdRef {
                odata_id: Some(odata_v4::Id(session_service_path + "/Sessions")),
            },
            ..Default::default()
        },
        ..Default::default()
    })
}

async fn computer_system_collection(
    uri: Uri,
    systems: Arc<Mutex<Vec<SimpleSystem>>>,
) -> impl IntoResponse {
    let length = systems.lock().unwrap().len();
    seuss::Response(ComputerSystemCollectionModel {
        odata_id: odata_v4::Id(uri.path().to_string()),
        members_odata_count: odata_v4::Count(length.try_into().unwrap()),
        members: (0..length)
            .map(|id| odata_v4::IdRef {
                odata_id: Some(odata_v4::Id(format!("{}/{}", uri.path(), id + 1))),
            })
            .collect(),
        name: resource::Name("Computer System Collection".to_string()),
        ..Default::default()
    })
}

async fn computer_system(
    uri: Uri,
    id: usize,
    systems: Arc<Mutex<Vec<SimpleSystem>>>,
) -> impl IntoResponse {
    seuss::Response(ComputerSystemModel {
        odata_id: odata_v4::Id(uri.path().to_string()),
        id: resource::Id(id.to_string()),
        name: resource::Name(format!("SimpleSystem-{}", id)),
        power_state: Some(systems.lock().unwrap().get(id - 1).unwrap().0.clone()),
        actions: Some(Actions {
            computer_system_reset: Some(Reset {
                target: Some(uri.path().to_string() + "/Actions/ComputerSystem.Reset"),
                ..Default::default()
            }),
            ..Default::default()
        }),
        ..Default::default()
    })
}

async fn computer_system_reset(
    id: usize,
    systems: Arc<Mutex<Vec<SimpleSystem>>>,
    request: ResetRequestBody,
) -> impl IntoResponse {
    let power_state = match request.reset_type.unwrap() {
        ResetType::On | ResetType::GracefulRestart => resource::PowerState::On,
        ResetType::ForceOff | ResetType::GracefulShutdown => resource::PowerState::Off,
        _ => {
            return Err(seuss::Response(error::one_message(
                Base::ActionParameterNotSupported("ResetType".to_string(), "Reset".to_string())
                    .into(),
            )))
        }
    };
    systems.lock().unwrap().get_mut(id - 1).unwrap().0 = power_state;
    Ok(StatusCode::NO_CONTENT)
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt::init();
    let args = Args::parse();
    let config: Configuration = serde_yaml::from_reader(File::open(&args.config)?)?;

    let authenticator = LinuxPamAuthenticator::new(config.role_map)?;
    let session_manager = InMemorySessionManager::new(authenticator.clone());
    let proxy = CombinedAuthenticationProxy::new(session_manager.clone(), authenticator);

    let systems: Vec<SimpleSystem> = vec![SimpleSystem(resource::PowerState::Off)];
    let systems = Arc::new(Mutex::new(systems));

    let odata = OData::new()
        .enable_account_service()
        .enable_session_service()
        .enable_sessions()
        .enable_systems()
        .into();

    let service_root = ServiceRoot::default()
    .get(service_root)
    .account_service(AccountService::new().into_router())
    .session_service(SessionService::new(session_manager, proxy.clone()).into_router())
    .systems(
        ComputerSystemCollection::default()
            .get({
                let systems = Arc::clone(&systems);
                |OriginalUri(uri): OriginalUri| async move {
                    computer_system_collection(uri, systems).await
                }
            })
            .computer_system(
                ComputerSystem::default()
                    .get({
                        let systems = Arc::clone(&systems);
                        |OriginalUri(uri): OriginalUri, Extension(id): Extension<usize>| async move {
                            computer_system(uri, id, systems).await
                    }})
                    .reset(|Extension(id): Extension<usize>, Json(request): Json<ResetRequestBody>| async move {
                        computer_system_reset(id, systems, request).await
                    })
                    .into_router()
                    .route_layer(ResourceLocator::new(
                        "computer_system_id".to_string(),
                        |id: usize| async move { Ok::<_, Response>(id) },
                    )),
            )
            .into_router(),
    )
    .into_router()
    .with_state(proxy);

    let app = RedfishService::new()
        .into_router(odata, service_root)
        .layer(TraceLayer::new_for_http());

    seuss::router::serve(config.server, app).await
}
