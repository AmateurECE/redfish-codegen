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

use redfish_codegen::models::{odata_v4, resource, service_root};
use redfish_codegen::api::v1;
use crate::endpoint::Endpoint;

#[derive(Clone)]
pub struct ServiceRoot {
    name: resource::Name,
    id: resource::Id,
    odata_id: odata_v4::Id,
}

impl ServiceRoot {
    pub fn new(name: resource::Name, id: resource::Id) -> Self {
        Self {
            name,
            id,
            odata_id: odata_v4::Id(String::default()),
        }
    }
}

impl Endpoint for ServiceRoot {
    fn mountpoint(&self) -> &str {
        &self.odata_id.0
    }

    fn mount(mut self, path: String) -> Self {
        self.odata_id.0 = path;
        self
    }
}

impl v1::ServiceRoot for ServiceRoot {
    fn get(&self) -> v1::ServiceRootGetResponse {
        let ServiceRoot {name, id, odata_id} = self.clone();
        v1::ServiceRootGetResponse::Ok(
            service_root::v1_15_0::ServiceRoot{
                name,
                id,
                odata_id,
                ..Default::default()
            })
    }
}

