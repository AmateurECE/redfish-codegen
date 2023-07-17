use axum::{
    extract::OriginalUri,
    http::{StatusCode, Uri},
    response::{IntoResponse, Response},
    Extension, Json,
};
use clap::Parser;
use seuss::{
    auth::{pam::LinuxPamAuthenticator, CombinedAuthenticationProxy, InMemorySessionManager},
    components::{
        computer_system::ComputerSystem, computer_system_collection::ComputerSystemCollection,
        odata::OData, service_root::ServiceRoot,
    },
    core::{error, privilege},
    middleware::ResourceLocator,
    models::{
        computer_system::v1_20_1::{
            Actions, ComputerSystem as ComputerSystemModel, Reset, ResetRequestBody,
        },
        computer_system_collection::ComputerSystemCollection as ComputerSystemCollectionModel,
        odata_v4,
        resource::{self, ResetType},
        service_root::v1_16_0::{Links, ServiceRoot as ServiceRootModel},
    },
    registries::{resource_event::v1_3_0::ResourceEvent, base::v1_16_0::Base},
    service::{AccountService, RedfishService, SessionService},
};
use std::{
    collections::HashMap,
    fs::File,
    sync::{Arc, Mutex},
};
use tower_http::trace::TraceLayer;
use tracing::{event, Level};

/// Command Line Interface, courtesy of the `clap` crate.
#[derive(Parser)]
struct Args {
    /// Configuration file
    #[clap(value_parser, short, long)]
    config: String,
}

/// The format of the yaml configuration file.
#[derive(serde::Deserialize)]
#[allow(dead_code)]
struct Configuration {
    /// Maps Redfish roles to POSIX group names. We are only required to
    /// provide this for use with the LinuxPamAuthenticator.
    #[serde(rename = "role-map")]
    role_map: HashMap<privilege::Role, String>,

    /// The seuss::router module provides this configuration struct to
    /// configure the server--TCP ports, paths to TLS certificates, etc.
    /// We put it right into our configuration object so that we can
    /// deserialize it from our yaml files.
    server: seuss::router::Configuration,
}

/// A "Simple System". The PowerState of this computer system can be mutated
/// using the ComputerSystem.Reset action, but it provides no other
/// functionality.
struct SimpleSystem(resource::PowerState);

/// This handler responds with a ServiceRoot instance to communicate
/// information about our service. It's intended to be wired up to handle
/// GET requests on the /redfish/v1/ URL.
async fn service_root(OriginalUri(uri): OriginalUri) -> impl IntoResponse {
    let session_service_path = uri.path().to_string() + "SessionService";

    // seuss::Response serializes our ServiceRootModel instance to JSON, and also adds some
    // required headers (Link, Cache-Control, etc.).
    seuss::Response(ServiceRootModel {
        odata_id: odata_v4::Id(uri.path().to_string()),
        id: resource::Id("simple".to_string()),
        name: resource::Name("Simple Redfish Service".to_string()),
        // This service exposes a Systems collection, so we indicate that here
        // to ensure its reachable by clients.
        systems: Some(odata_v4::IdRef {
            odata_id: Some(odata_v4::Id(uri.path().to_string() + "Systems")),
        }),

        // These links are required for full compliance. We don't fill them
        // in automatically, because the author of the service is responsible
        // for ensuring that these services are constructed and exposed.
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

/// This handler responds with a ComputerSystemCollection entity, which
/// exposes the set of SimpleSystem resources.
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

/// This handler responds with a ComputerSystem entity representing an
/// instance of a SimpleSystem.
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

        // The SimpleSystem supports the ComputerSystem.Reset action.
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

/// Implements the ComputerSystem.Reset action for a SimpleSystem instance.
/// Since the SimpleSystems are all behind atomically reference-counted
/// mutexes, changes made to instances persist for the lifespan of the service.
async fn computer_system_reset(
    id: usize,
    systems: Arc<Mutex<Vec<SimpleSystem>>>,
    request: ResetRequestBody,
) -> impl IntoResponse {
    let power_state = match request.reset_type.unwrap() {
        ResetType::On => {
            resource::PowerState::On
        },
        ResetType::ForceOff | ResetType::GracefulShutdown => {
            resource::PowerState::Off
        },
        _ => {
            // If the requested power state is not one of the handled variants, return a
            // redfish error.
            return Err(seuss::Response(error::one_message(
                Base::ActionParameterNotSupported("ResetType".to_string(), "Reset".to_string())
                    .into(),
            )));
        }
    };

    let name = format!("SimpleSystem-{}", id);
    let event = match power_state {
        resource::PowerState::On => ResourceEvent::ResourcePoweredOn(name),
        resource::PowerState::Off => ResourceEvent::ResourcePoweredOff(name),
        resource::PowerState::PoweringOff => ResourceEvent::ResourcePoweringOff(name),
        resource::PowerState::PoweringOn => ResourceEvent::ResourcePoweringOn(name),
        resource::PowerState::Paused => ResourceEvent::ResourcePaused(name),
    };
    event!(Level::INFO, ?event);
    systems.lock().unwrap().get_mut(id - 1).unwrap().0 = power_state;
    Ok(StatusCode::NO_CONTENT)
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    // Create a formatted log stream on stdout, parse our CLI parameters, read our
    // configuration.
    tracing_subscriber::fmt::init();
    let args = Args::parse();
    let config: Configuration = serde_yaml::from_reader(File::open(&args.config)?)?;

    // The LinuxPamAuthenticator uses libpam to authenticate users with the credentials
    // provided in HTTP requests. It will also attempt to open a PAM session for the
    // SessionService.
    let authenticator = LinuxPamAuthenticator::new(config.role_map)?;

    // The InMemorySessionManager stores sessions in memory, so when the service dies,
    // all sessions are cleaned up.
    let session_manager = InMemorySessionManager::new(authenticator.clone());

    // The CombinedAuthenticationProxy uses our LinuxPamAuthenticator and InMemorySessionManager
    // to authenticate users and authorize every HTTP request for both basic and session
    // authentication. There is also a BasicAuthenticationProxy if only Basic authentication
    // support is desired. These types are generic over the SessionManagement and
    // {Basic,Session}Authentication traits, so a service author could easily write an
    // implementation of one or both of these to use instead.
    let proxy = CombinedAuthenticationProxy::new(session_manager.clone(), authenticator);

    // Create the collection of SimpleSystems.
    let systems: Vec<SimpleSystem> = vec![SimpleSystem(resource::PowerState::Off)];
    let systems = Arc::new(Mutex::new(systems));

    // Create an OData service document for our service, which exposes links to the Account,
    // Session, and Systems services.
    let odata = OData::new()
        .enable_account_service()
        .enable_session_service()
        .enable_sessions()
        .enable_systems()
        .into();

    // Create a ServiceRoot component. Handlers for responding to requests and generating entities
    // are attached to Components. Here, we attach a "ComputerSystemCollection" component via the
    // ServiceRoot::systems() method. The ServiceRoot component takes care of serving these
    // sub-routes relative to "/redfish/v1/Systems". Components are also convertible to
    // axum::Router, so that they can integrate seamlessly with the rest of the axum ecosystem.
    let service_root = ServiceRoot::default()
        .get(service_root)
        // These AccountService and SessionService implementations are provided standard as part of the
        // seuss crate. The seuss crate provides implementations for some of the boring services.
        .account_service(AccountService::default().into_router())
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
                        // A ResourceLocator is middleware that converts path parameters to data that
                        // are required to locate instances of entities within handlers. The data are
                        // passed as Extensions. This one isn't particularly interesting, but in more
                        // exotic systems, it could be used to perform RPC or REST calls to upstream or
                        // remote services. The parameter name is taken from the Redfish OpenAPI
                        // document--by convention, it's the name of the entity (in snake case)
                        // suffixed with "_id".
                        .route_layer(ResourceLocator::new(
                            "computer_system_id".to_string(),
                            |id: usize| async move { Ok::<_, Response>(id) },
                        )),
                )
                .into_router(),
        )
        .into_router()
        .with_state(proxy);

    // Attach our ServiceRoot and OData service document to a redfish service. The RedfishService
    // takes care of setting up required redirects and also serves an OData metadata document.
    let app = RedfishService::default()
        .into_router(odata, service_root)
        .layer(TraceLayer::new_for_http());

    // Finally, use the seuss::router module to serve our redfish service. This takes a
    // seuss::router::Configuration instance (which we deserialize from yaml), and an axum::Router
    // (generated from the RedfishService). It will redirect HTTP requests to the HTTPS server, and
    // do some other setup that would be boring to write by hand.
    seuss::router::serve(config.server, app).await
}
