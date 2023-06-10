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

use redfish_codegen::models::message::v1_1_2::Message;
use redfish_codegen::models::redfish::{Error, RedfishError};

pub fn one_message(error: Message) -> Error {
    let message = error
        .message
        .as_ref()
        .map(|m| m.as_str())
        .unwrap_or("")
        .to_string();
    let code = error.message_id.clone();
    Error {
        error: RedfishError {
            message_extended_info: Some(vec![error]),
            message,
            code,
        },
    }
}
