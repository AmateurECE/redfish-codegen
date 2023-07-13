use std::{
    convert::Infallible,
    future::Future,
    pin::Pin,
    task::{Context, Poll},
};

use axum::{
    async_trait,
    body::Body,
    extract::FromRequestParts,
    http::{HeaderValue, Request, StatusCode},
    response::{IntoResponse, Response},
    routing::Route,
    Json,
};
use redfish_codegen::{registries::base::v1_16_0::Base, ODATA_VERSION};
use redfish_core::error;
use tower::{Layer, Service};

const ODATA_VERSION_HEADER: &str = "OData-Version";

#[derive(Clone, Debug)]
pub struct ODataVersionRejection;

impl IntoResponse for ODataVersionRejection {
    fn into_response(self) -> Response {
        (
            StatusCode::PRECONDITION_FAILED,
            Json(error::one_message(
                Base::HeaderInvalid(ODATA_VERSION_HEADER.to_string()).into(),
            )),
        )
            .into_response()
    }
}

#[derive(Clone, Debug)]
pub struct ODataVersion(pub Option<String>);

#[async_trait]
impl<S> FromRequestParts<S> for ODataVersion
where
    S: Send + Clone,
{
    type Rejection = ODataVersionRejection;

    async fn from_request_parts(
        parts: &mut axum::http::request::Parts,
        _state: &S,
    ) -> Result<Self, Self::Rejection> {
        let odata_version = parts
            .headers
            .get(ODATA_VERSION_HEADER)
            .and_then(|odata_version| odata_version.to_str().ok())
            .map(|value| value.to_string());
        Ok(ODataVersion(odata_version))
    }
}

#[derive(Clone)]
pub struct ODataLayer;

impl Layer<Route> for ODataLayer {
    type Service = ODataService;

    fn layer(&self, inner: Route) -> Self::Service {
        ODataService(inner)
    }
}

#[derive(Clone)]
pub struct ODataService(Route);

impl Service<Request<Body>> for ODataService {
    type Response = Response;
    type Error = Infallible;
    type Future = Pin<Box<dyn Future<Output = Result<Self::Response, Self::Error>> + Send>>;

    fn poll_ready(&mut self, cx: &mut Context<'_>) -> Poll<Result<(), Self::Error>> {
        self.0.poll_ready(cx)
    }

    fn call(&mut self, request: Request<Body>) -> Self::Future {
        let mut inner = self.0.clone();
        let handler = async move {
            let (mut parts, body) = request.into_parts();
            let odata_version = match ODataVersion::from_request_parts(&mut parts, &()).await {
                Ok(result) => result.0,
                Err(error) => return Ok::<_, Infallible>(error.into_response()),
            };

            if odata_version.is_some() && odata_version.as_deref() != Some(ODATA_VERSION) {
                return Ok::<_, Infallible>(ODataVersionRejection.into_response());
            }

            let request = Request::from_parts(parts, body);
            let mut response = inner.call(request).await.unwrap();
            response.headers_mut().insert(
                ODATA_VERSION_HEADER,
                HeaderValue::from_static(ODATA_VERSION),
            );
            Ok(response)
        };
        Box::pin(handler)
    }
}
