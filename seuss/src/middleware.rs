use std::{
    collections::HashMap, convert::Infallible, future::Future, marker::PhantomData, pin::Pin,
    str::FromStr,
};

use axum::{
    async_trait,
    body::Body,
    extract::{FromRequestParts, Path},
    http::{request::Parts, Request},
    response::{IntoResponse, Response},
    routing::Route,
};

use crate::error::{redfish_map_err, redfish_map_err_no_log};

async fn get_request_parameter<T>(
    mut parts: &mut Parts,
    parameter_name: &String,
) -> Result<T, Response>
where
    T: FromStr,
{
    Path::<HashMap<String, String>>::from_request_parts(&mut parts, &())
        .await
        .map_err(|rejection| rejection.into_response())
        .and_then(|parameters| {
            parameters
                .get(parameter_name)
                .ok_or_else(|| {
                    redfish_map_err("Missing '".to_string() + parameter_name + "' parameter")
                })
                .map(|parameter| parameter.clone())
        })
        .and_then(|value| T::from_str(&value).map_err(redfish_map_err_no_log))
}

#[derive(Clone)]
pub struct FunctionResourceHandler<Input, F> {
    f: F,
    marker: PhantomData<fn() -> Input>,
}

#[async_trait]
pub trait ResourceHandler {
    async fn call(
        self,
        request: Request<Body>,
        parameter_name: String,
    ) -> Result<Request<Body>, Response>;
}

#[async_trait]
impl<T1, T2, Fn, Fut, R> ResourceHandler for FunctionResourceHandler<(T1, T2), Fn>
where
    T1: FromRequestParts<()> + Send,
    T2: FromStr + Send,
    Fn: FnOnce(T1, T2) -> Fut + Send,
    Fut: Future<Output = Result<R, Response>> + Send,
    R: Send + Sync + 'static,
{
    async fn call(
        self,
        request: Request<Body>,
        parameter_name: String,
    ) -> Result<Request<Body>, Response> {
        let (mut parts, body) = request.into_parts();
        let extractor = T1::from_request_parts(&mut parts, &())
            .await
            .map_err(|rejection| rejection.into_response())?;
        let parameter = get_request_parameter::<T2>(&mut parts, &parameter_name)
            .await
            .and_then(|value| Ok((self.f)(extractor, value)))?
            .await?;

        let mut request = Request::<Body>::from_parts(parts, body);
        request.extensions_mut().insert(parameter);
        Ok(request)
    }
}

#[async_trait]
impl<T, Fn, Fut, R> ResourceHandler for FunctionResourceHandler<(T,), Fn>
where
    T: FromStr + Send,
    Fn: FnOnce(T) -> Fut + Send,
    Fut: Future<Output = Result<R, Response>> + Send,
    R: Send + Sync + 'static,
{
    async fn call(
        self,
        request: Request<Body>,
        parameter_name: String,
    ) -> Result<Request<Body>, Response> {
        let (mut parts, body) = request.into_parts();
        let parameter = get_request_parameter(&mut parts, &parameter_name)
            .await
            .and_then(|value| Ok((self.f)(value)))?
            .await?;

        let mut request = Request::<Body>::from_parts(parts, body);
        request.extensions_mut().insert(parameter);
        Ok(request)
    }
}

pub trait IntoResourceHandler<Input> {
    type ResourceHandler;
    fn into_resource_handler(self) -> Self::ResourceHandler;
}

impl<T1, T2, F, R> IntoResourceHandler<(T1, T2)> for F
where
    T1: FromRequestParts<()>,
    T2: FromStr,
    F: FnOnce(T1, T2) -> R,
{
    type ResourceHandler = FunctionResourceHandler<(T1, T2), F>;

    fn into_resource_handler(self) -> Self::ResourceHandler {
        Self::ResourceHandler {
            f: self,
            marker: PhantomData::default(),
        }
    }
}

impl<T, F, R> IntoResourceHandler<(T,)> for F
where
    T: FromStr,
    F: FnOnce(T) -> R,
{
    type ResourceHandler = FunctionResourceHandler<(T,), F>;

    fn into_resource_handler(self) -> Self::ResourceHandler {
        Self::ResourceHandler {
            f: self,
            marker: PhantomData::default(),
        }
    }
}

#[derive(Clone)]
pub struct ResourceLocator<R>
where
    R: ResourceHandler + Clone,
{
    parameter_name: String,
    handler: R,
}

impl<R> ResourceLocator<R>
where
    R: ResourceHandler + Clone,
{
    pub fn new<I>(
        parameter_name: String,
        handler: impl IntoResourceHandler<I, ResourceHandler = R>,
    ) -> Self {
        Self {
            parameter_name,
            handler: handler.into_resource_handler(),
        }
    }
}

impl<R> tower::Layer<Route> for ResourceLocator<R>
where
    R: ResourceHandler + Clone,
{
    type Service = ResourceLocatorService<R>;

    fn layer(&self, inner: Route) -> Self::Service {
        ResourceLocatorService {
            inner,
            handler: self.handler.clone(),
            parameter_name: self.parameter_name.clone(),
        }
    }
}

#[derive(Clone)]
pub struct ResourceLocatorService<R>
where
    R: ResourceHandler,
{
    inner: Route,
    handler: R,
    parameter_name: String,
}

impl<R> tower::Service<Request<Body>> for ResourceLocatorService<R>
where
    R: ResourceHandler + Send + Sync + Clone + 'static,
{
    type Response = Response;

    type Error = Infallible;

    type Future = Pin<Box<dyn Future<Output = Result<Self::Response, Self::Error>> + Send>>;

    fn poll_ready(
        &mut self,
        cx: &mut std::task::Context<'_>,
    ) -> std::task::Poll<Result<(), Self::Error>> {
        self.inner.poll_ready(cx)
    }

    fn call(&mut self, request: Request<Body>) -> Self::Future {
        let mut inner = self.inner.clone();
        let parameter_name = self.parameter_name.clone();
        let handler = self.handler.clone();
        let handler = async move {
            let request = match handler.call(request, parameter_name).await {
                Ok(value) => value,
                Err(rejection) => return Ok::<_, Infallible>(rejection),
            };
            let response = inner.call(request).await;
            response
        };
        Box::pin(handler)
    }
}
