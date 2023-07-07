use axum::{response::Redirect, routing::MethodRouter, Router};
use redfish_axum::metadata::Metadata;

use crate::middleware::ODataLayer;

use super::RedfishVersions;

#[derive(Default)]
pub struct RedfishService;

impl RedfishService {
    pub fn into_router(self, odata: MethodRouter, service_root: Router) -> Router {
        Router::new()
            .route("/redfish", RedfishVersions::default().into())
            .route(
                "/redfish/v1",
                axum::routing::get(|| async { Redirect::permanent("/redfish/v1/") }),
            )
            .route("/redfish/v1/odata", odata)
            .route("/redfish/v1/$metadata", Metadata.into())
            .nest("/redfish/v1/", service_root.route_layer(ODataLayer))
    }
}
