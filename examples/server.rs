///////////////////////////////////////////////////////////////////////////////
// NAME:            server.rs
//
// AUTHOR:          Ethan D. Twardy <ethan.twardy@gmail.com>
//
// DESCRIPTION:     This example implements a proof-of-concept server with
//                  an UpdateService.
//
// CREATED:         02/17/2023
//
// LAST EDITED:	    02/18/2023
//
//////

use axum::{routing::get, Router};

#[tokio::main]
async fn main() {
    let app = Router::new().route("/", get(|| async { "Hello, World!" }));

    axum::Server::bind(&"0.0.0.0:3000".parse().unwrap())
        .serve(app.into_make_service())
        .await
        .unwrap();
}

///////////////////////////////////////////////////////////////////////////////
