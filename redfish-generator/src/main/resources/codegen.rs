/// The OData Version supported by this version of the redfish data model.
pub const ODATA_VERSION: &str = "4.0";

/// The base URL of the Redfish json-schema files.
pub const SCHEMA_BASE_URL: &str = "https://redfish.dmtf.org/schemas/v1";

/// Metadata about a model.
pub trait Metadata<'a> {
    /// Name of the json-schema file that describes the entity that implements this trait. Should
    /// be only the file name, so that it can be resolved relative to the URL of the redfish
    /// service, or the public Redfish schema index.
    const JSON_SCHEMA: &'a str;
}
