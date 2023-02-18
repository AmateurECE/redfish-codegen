
use crate::models;

///////////////////////////////////////////////////////////////////////////////
// UpdateServiceActionSimpleUpdate
////

pub enum PostResponse {
    Ok(models::redfish::RedfishError),
    Created(models::redfish::RedfishError),
    Accepted(models::task::v1_5_1::Task),
    NoContent,
}

pub trait SimpleUpdate {
    const URL: &'static str = "/redfish/v1/UpdateService/Actions/UpdateService.SimpleUpdate";
    fn post(&self) -> Result<PostResponse, models::redfish::RedfishError>;
}
