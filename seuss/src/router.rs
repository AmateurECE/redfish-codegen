// Author: Ethan D. Twardy <ethan.twardy@gmail.com>
//
// Copyright 2023, Ethan Twardy. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the \"License\");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an \"AS IS\" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

#[derive(Copy, Clone)]
#[cfg_attr(feature = "serde", derive(serde::Deserialize))]
struct Ports {
    http: u16,
    https: u16,
}

#[cfg_attr(feature = "serde", derive(serde::Deserialize))]
pub struct Configuration {
    address: String,
    ports: Ports,
    #[cfg_attr(feature = "serde", serde(rename = "certificate-file"))]
    certificate_file: String,
    #[cfg_attr(feature = "serde", serde(rename = "key-file"))]
    key_file: String,
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
