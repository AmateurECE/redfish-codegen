use axum::{
    extract::State,
    routing::{get, MethodRouter},
    Json,
};
use std::collections::HashMap;

pub struct RedfishVersions(MethodRouter);

impl RedfishVersions {
    pub fn new(versions: HashMap<String, String>) -> Self {
        let router = get(|State(state): State<HashMap<String, String>>| async move { Json(state) })
            .with_state(versions);
        Self(router)
    }
}

impl Default for RedfishVersions {
    fn default() -> Self {
        let mut version_map = HashMap::new();
        version_map.insert("v1".to_string(), "/redfish/v1/".to_string());
        RedfishVersions::new(version_map)
    }
}

impl From<RedfishVersions> for MethodRouter {
    fn from(val: RedfishVersions) -> Self {
        val.0
    }
}
