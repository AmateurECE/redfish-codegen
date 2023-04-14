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

impl Into<MethodRouter> for RedfishVersions {
    fn into(self) -> MethodRouter {
        self.0
    }
}
