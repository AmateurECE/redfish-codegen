// The FirmwareInventoryCollection provides the following URLs:
//  /redfish/v1/UpdateService/FirmwareInventory
//  /redfish/v1/UpdateService/FirmwareInventory/{SoftwareInventoryId}

pub trait UpdateServiceFirmwareInventory {
    const URL: &'static str = "/redfish/v1/UpdateService/FirmwareInventory";
}
