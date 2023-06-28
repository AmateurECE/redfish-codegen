use axum::{response::IntoResponse, Json};
use redfish_codegen::{Metadata, SCHEMA_BASE_URL};

/// Constructs an HTTP response with the Link header set to the path of the
/// json-schema document describing the Redfish model contained in the response
/// body.
pub struct Link<M>(
    /// The base URL of the json-schema document. This can be used to refer the
    /// json-schema client to a relative path on the current service, or (the
    /// default behavior with the use of [crate::Response]) relative to the
    /// public Redfish schema index.
    pub &'static str,
    /// The model instance forming the response body.
    pub M,
);

impl<M> IntoResponse for Link<M>
where
    M: Metadata<'static>,
    Json<M>: IntoResponse,
{
    fn into_response(self) -> axum::response::Response {
        (
            [(
                "Link",
                "<".to_string() + self.0 + "/" + M::JSON_SCHEMA + ">; rel=describedby",
            )],
            Json(self.1),
        )
            .into_response()
    }
}

/// Constructs an HTTP response using the provided Redfish model instance. This
/// creates a request with the following:
/// 1. The Link header is set with the URL to the json-schema of the model
///    contained in the request body.
/// 2. The Cache-Control header is set to "no-cache".
/// 3. The response body is serialized to JSON.
pub struct Response<M>(pub M);

impl<M> IntoResponse for Response<M>
where
    M: Metadata<'static>,
    Json<M>: IntoResponse,
{
    fn into_response(self) -> axum::response::Response {
        (
            [("Cache-Control", "no-cache")],
            Link(SCHEMA_BASE_URL, self.0),
        )
            .into_response()
    }
}
