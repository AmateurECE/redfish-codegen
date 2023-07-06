use redfish_codegen::models::message::v1_1_2::Message;
use redfish_codegen::models::redfish::{Error, RedfishError};

pub fn one_message(error: Message) -> Error {
    let message = error.message.as_deref().unwrap_or("").to_string();
    let code = error.message_id.clone();
    Error {
        error: RedfishError {
            message_extended_info: Some(vec![error]),
            message,
            code,
        },
    }
}
