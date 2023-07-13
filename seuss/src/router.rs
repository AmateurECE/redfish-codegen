use std::{net::SocketAddr, path::PathBuf};

use axum::{
    extract::Host,
    handler::HandlerWithoutStateExt,
    http::{StatusCode, Uri},
    response::Redirect,
    BoxError, Router,
};
use axum_server::{tls_rustls::RustlsConfig, Handle};
use futures::{FutureExt, StreamExt};
use signal_hook::consts::{SIGINT, SIGTERM};
use signal_hook_tokio::Signals;

/// The ports bound by the service.
#[derive(Copy, Clone)]
#[cfg_attr(feature = "serde", derive(serde::Deserialize))]
pub struct Ports {
    /// The port to listen for HTTP requests.
    pub http: u16,
    /// The port to listen for HTTPS requests.
    pub https: u16,
}

/// Configuration for the service.
#[cfg_attr(feature = "serde", derive(serde::Deserialize))]
pub struct Configuration {
    /// The IPv4 address to listen for connections on.
    pub address: String,
    /// The ports to listen on.
    pub ports: Ports,
    /// The path to a TLS certificate file for the server. Not all container formats are supported,
    /// but RFC 1422 PEM (.pem) files _are_ supported. In that case, this file should contain a
    /// full certificate chain for the service.
    #[cfg_attr(feature = "serde", serde(rename = "certificate-file"))]
    pub certificate_file: String,
    /// The path to a TLS key chain for the server. Not all container formats are supported, but
    /// RFC 1422 PEM (.pem) files _are_ supported. In that case, it should contain an encoded
    /// private key.
    #[cfg_attr(feature = "serde", serde(rename = "key-file"))]
    pub key_file: String,
}

async fn redirect_http_to_https(address: String, ports: Ports) {
    fn make_https(host: String, uri: Uri, ports: Ports) -> Result<Uri, BoxError> {
        let mut parts = uri.into_parts();

        parts.scheme = Some(axum::http::uri::Scheme::HTTPS);

        if parts.path_and_query.is_none() {
            parts.path_and_query = Some("/".parse().unwrap());
        }

        let https_host = host.replace(&ports.http.to_string(), &ports.https.to_string());
        parts.authority = Some(https_host.parse()?);

        Ok(Uri::from_parts(parts)?)
    }

    let redirect = move |Host(host): Host, uri: Uri| async move {
        match make_https(host, uri, ports) {
            Ok(uri) => Ok(Redirect::permanent(&uri.to_string())),
            Err(error) => {
                tracing::warn!(%error, "failed to convert URI to HTTPS");
                Err(StatusCode::BAD_REQUEST)
            }
        }
    };

    let addr: SocketAddr = (address + ":" + &ports.http.to_string()).parse().unwrap();
    tracing::debug!("http redirect listening on {}", addr);

    axum::Server::bind(&addr)
        .serve(redirect.into_make_service())
        .await
        .unwrap();
}

/// Exposes the service (described by the [axum::Router]) over HTTPS using the provided
/// configuration. Also starts an HTTP service that responds to all requests with a 301 Permanent
/// Redirect to the same URL with the https:// scheme.
pub async fn serve(config: Configuration, app: Router) -> anyhow::Result<()> {
    let server_handle = Handle::new();
    let signals = Signals::new([SIGINT, SIGTERM])?;
    let signals_handle = signals.handle();
    let signal_handler = |mut signals: Signals| async move {
        if signals.next().await.is_some() {
            println!("Gracefully shutting down");
            server_handle.shutdown();
        }
    };

    let tls_config = RustlsConfig::from_pem_file(
        PathBuf::from(config.certificate_file),
        PathBuf::from(config.key_file),
    )
    .await
    .unwrap();

    let https_address = config.address.clone() + ":" + &config.ports.https.to_string();

    let signals_task = tokio::spawn(signal_handler(signals)).fuse();
    let https_server = axum_server::bind_rustls(https_address.parse().unwrap(), tls_config)
        .serve(app.into_make_service())
        .fuse();
    let http_server = tokio::spawn(redirect_http_to_https(config.address, config.ports)).fuse();

    futures::pin_mut!(signals_task, https_server, http_server);
    futures::select! {
        _ = https_server => {},
        _ = http_server => {},
        _ = signals_task => {},
    };

    signals_handle.close();
    Ok(())
}
