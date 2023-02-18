// The SoftwareInventoryCollection provides the following URLs:
//  /redfish/v1/UpdateService/SoftwareInventory
//  /redfish/v1/UpdateService/SoftwareInventory/{SoftwareInventoryId}

use crate::models;

pub trait UpdateServiceSoftwareInventory {
    const URL: &'static str = "/redfish/v1/UpdateService/SoftwareInventory";
}

pub trait SoftwareInventoryService: Into<models::software_inventory::v1_9_0::SoftwareInventory> {}

pub trait SoftwareInventoryCollectionService: Into<models::software_inventory_collection::SoftwareInventoryCollection> {
    type MemberService: SoftwareInventoryService;

    fn get_member(id: i64) -> Self::MemberService;
}
