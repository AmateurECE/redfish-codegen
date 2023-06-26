/// The base URL of the Redfish json-schema files.
pub const SCHEMA_BASE_URL: &str = "https://redfish.dmtf.org/schemas/v1";

/// Metadata about a model.
pub trait Metadata<'a> {
    const JSON_SCHEMA: &'a str;
}
