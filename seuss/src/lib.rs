//! Seuss is a set of components and entities for implementing Redfish-compliant services. This
//! crate combines the [redfish_codegen] and [redfish_axum] crates--which are generated
//! programmatically from the Redfish schema bundle and standard registries--with some basic
//! implementations of common services and components to provide a batteries-included suite of
//! tools for implementing Redfish compliant services on POSIX and non-POSIX systems.

/// Components for authenticating and authorizing users.
pub mod auth;
/// Utilities for reporting Redfish errors to users.
pub mod error;
/// Middleware that can be used with the axum ecosystem (mostly used internally).
pub mod middleware;
/// Implementations of some standard components.
pub mod service;

/// Utilities to easily expose an [axum::Router] over HTTP.
#[cfg(feature = "router")]
pub mod router;

mod model;
pub use model::*;

pub use redfish_codegen::*;

/// Re-exports from [redfish_axum].
pub mod components {
    pub use redfish_axum::*;
}

/// Re-exports from [redfish_core].
pub mod core {
    pub use redfish_core::*;
}
