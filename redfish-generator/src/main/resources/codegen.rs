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

/// Metadata for an item in a registry
pub trait Registry<'a> {
    /// Composes this registry items message, consuming its arguments and the instance itself in the process.
    fn message(self) -> String;

    /// Creates a vector of this instances arguments in string representation.
    fn args(&self) -> Option<Vec<String>>;

    /// Returns the severity associated with this registry item.
    fn severity(&self) -> crate::models::resource::Health;

    /// Obtain a reference to this registry items unique identifier.
    fn id(&self) -> &'a str;

    /// Obtain a reference to a message that indicates how to resolve the condition indicated by this registry item,
    /// if resolution is required.
    fn resolution(&self) -> &'a str;
}
