use axum::{
    http::StatusCode,
    response::{IntoResponse, Response},
    Json,
};
use redfish_codegen::registries::base::v1_16_0::Base;
use redfish_core::error;
use tracing::{event, Level};

pub fn redfish_map_err<E>(error: E) -> Response
where
    E: std::fmt::Display,
{
    event!(Level::ERROR, "{}", &error);
    redfish_map_err_no_log(error)
}

pub fn redfish_map_err_no_log<E>(_: E) -> Response {
    (
        StatusCode::BAD_REQUEST,
        Json(error::one_message(Base::InternalError.into())),
    )
        .into_response()
}
