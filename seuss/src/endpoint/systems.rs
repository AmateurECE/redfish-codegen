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

use crate::endpoint::Endpoint;
use crate::redfish_error;
use redfish_codegen::api::v1::systems;
use redfish_codegen::models::{
    computer_system::v1_20_0::ComputerSystem, computer_system_collection::ComputerSystemCollection,
    resource,
};
use redfish_codegen::registries::base::v1_15_0::Base;

#[derive(Clone)]
pub struct Systems {
    odata_id: resource::Id,
}

impl systems::Systems for Systems {
    fn get(&self) -> systems::SystemsGetResponse {
        systems::SystemsGetResponse::Ok(ComputerSystemCollection {
            ..Default::default()
        })
    }

    fn post(&mut self, _body: ComputerSystem) -> systems::SystemsPostResponse {
        systems::SystemsPostResponse::Default(redfish_error::one_message(
            Base::QueryNotSupportedOnResource.into(),
        ))
    }
}

impl Endpoint for Systems {
    fn mountpoint(&self) -> &str {
        &self.odata_id.0
    }

    fn mount(mut self, path: String) -> Self {
        self.odata_id.0 = path;
        self
    }
}
