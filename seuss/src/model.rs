use axum::{response::IntoResponse, Json};
use redfish_codegen::{Metadata, SCHEMA_BASE_URL};

pub struct Link<M>(pub &'static str, pub M);

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

pub struct Response<M>(pub M);

impl<M> IntoResponse for Response<M>
where
    M: Metadata<'static>,
    Json<M>: IntoResponse,
{
    fn into_response(self) -> axum::response::Response {
        Link(SCHEMA_BASE_URL, self.0).into_response()
    }
}
