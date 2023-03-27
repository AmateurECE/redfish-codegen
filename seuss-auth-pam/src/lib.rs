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

use std::error;
use std::fmt;

pub(crate) const DEFAULT_PAM_SERVICE: &str = "redfish";

#[derive(Debug)]
pub struct MissingGroupError(String);
impl fmt::Display for MissingGroupError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Group {} does not exist", &self.0)
    }
}

impl error::Error for MissingGroupError {
    fn source(&self) -> Option<&(dyn error::Error + 'static)> {
        None
    }
}

mod basic;
pub use basic::*;
