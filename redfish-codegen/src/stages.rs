//! The stages that the model inhabits throughout code generation.

use std::path::PathBuf;

/// A valid Redfish version.
pub struct RedfishVersion(String);

/// Context identifying the specification data on disk.
pub struct Specification {
    spec_directory: PathBuf,
    spec_version: RedfishVersion,
    registry_directory: PathBuf,
}
