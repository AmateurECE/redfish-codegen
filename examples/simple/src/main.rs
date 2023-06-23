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

use axum::{Json, Router};
use clap::Parser;
use redfish_axum::{
    computer_system_collection::ComputerSystemCollection, service_root::ServiceRoot,
};
use redfish_codegen::models::{
    computer_system_collection::ComputerSystemCollection as ComputerSystemCollectionModel,
    service_root::v1_15_0::ServiceRoot as ServiceRootModel,
};
use redfish_core::privilege::Role;
use seuss::{auth::NoAuth, service::redfish_versions::RedfishVersions};
use std::{collections::HashMap, fs::File};
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
    role_map: HashMap<Role, String>,
    server: seuss::router::Configuration,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt::init();
    let args = Args::parse();
    let config: Configuration = serde_yaml::from_reader(File::open(&args.config)?)?;

    // let sessions: &'static str = "/redfish/v1/SessionService/Sessions";
    // let authenticator = LinuxPamAuthenticator::new(config.role_map)?;
    // let session_collection =
    //     InMemorySessionManager::new(authenticator.clone(), odata_v4::Id(sessions.to_string()));
    // let proxy = CombinedAuthenticationProxy::new(session_collection.clone(), authenticator);

    let app = Router::new()
        .route("/redfish", RedfishVersions::default().into())
        .nest(
            "/redfish/v1/",
            ServiceRoot::default()
                .get(|| async { Json(ServiceRootModel::default()) })
                .systems(
                    ComputerSystemCollection::default()
                        .get(|| async { Json(ComputerSystemCollectionModel::default()) })
                        .into_router(),
                )
                .into_router()
                .with_state(NoAuth),
        )
        .layer(TraceLayer::new_for_http());

    seuss::router::serve(config.server, app).await
}
