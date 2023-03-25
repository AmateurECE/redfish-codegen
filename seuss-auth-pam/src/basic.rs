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

use crate::DEFAULT_PAM_SERVICE;
use libc::{c_int, c_void};
use pam_sys::{
    wrapped as pam, PamConversation, PamHandle, PamMessage, PamResponse, PamReturnCode, PamFlag
};
use redfish_codegen::{models::redfish, registries::base::v1_15_0::Base};
use seuss::{
    auth::{AuthenticatedUser, BasicAuthentication},
    redfish_error,
};
use std::ptr;

#[derive(Clone)]
pub struct LinuxPamBasicAuthenticator {
    service: String,
}

impl LinuxPamBasicAuthenticator {
    pub fn new() -> Self {
        LinuxPamBasicAuthenticator {
            service: DEFAULT_PAM_SERVICE.to_string(),
        }
    }

    fn terminate(&self, handle: &mut PamHandle, result: PamReturnCode) -> redfish::Error {
        pam::end(handle, result);
        unauthorized()
    }
}

fn unauthorized() -> redfish::Error {
    redfish_error::one_message(Base::InsufficientPrivilege.into())
}

extern "C" fn conversation_handler(
    number_of_messages: c_int,
    messages: *mut *mut PamMessage,
    responses: *mut *mut PamResponse,
    data: *mut c_void,
) -> c_int {
    todo!()
}

impl BasicAuthentication for LinuxPamBasicAuthenticator {
    fn authenticate(
        &self,
        username: String,
        mut password: String,
    ) -> Result<AuthenticatedUser, redfish::Error> {
        let mut handle: *mut PamHandle = ptr::null_mut();
        let conversation = PamConversation {
            conv: Some(conversation_handler),
            data_ptr: &mut password as *mut String as *mut c_void,
        };

        // Initialize the PamHandle structure
        // TODO: LinuxPamBasicAuthenticator should own the PamHandle, not initialize it on every request.
        let result = pam::start(&self.service, Some(&username), &conversation, &mut handle);
        if result != PamReturnCode::SUCCESS {
            // TODO: Better logging here?
            return Err(unauthorized());
        }

        // TODO: Implement a delay here.
        unsafe {
            let result = pam::authenticate(&mut *handle, PamFlag::DISALLOW_NULL_AUTHTOK);
            if result != PamReturnCode::SUCCESS {
                return Err(self.terminate(&mut *handle, result));
            }
        }

        Err(unauthorized())
    }
}
