// The UpdateService provides the following URLs:
//  /redfish/v1/UpdateService
//  /redfish/v1/UpdateService/Actions/UpdateService.SimpleUpdate
//  /redfish/v1/UpdateService/Actions/UpdateService.StartUpdate

use crate::models;

pub mod client_certificates;
pub mod firmware_inventory;
pub mod remote_server_certificates;
pub mod software_inventory;
pub mod actions;

// Note: Response enum not created for this trait since there is only one valid
// response besides the "default".
pub trait UpdateService {
    const URL: &'static str = "/redfish/v1/UpdateService";
    fn get(&self) -> Result<models::update_service::v1_11_2::UpdateService, models::redfish::RedfishError>;
}
