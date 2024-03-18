# The Redfish Codegen Project

This project aims to develop tools which are used to translate the Redfish
specification into Rust code. The primary crate provided by this repository
is `redfish-models`, which contains an unopinionated translation of the
Redfish Schema Bundle (DSP8010) and the Redfish Base Registries Specification
(DSP8011).

See the [Rust docs][1] for more information.

# Building

Currently, the `build.rs` script for the redfish-models project invokes
`make(1)` with the makefile script in the root of the repository. This script
depends on the following utilities to be available in $PATH:

 * `curl(1)`
 * `unzip(1)`
 * `quilt(1)`
 * `java(1)` (JDK 17 or greater)
 * `mvn`
 * `sed(1)`

[1]: https://docs.rs/redfish-models

# Licensing

The `redfish-generator` application (the Java code) is licensed under
Apache-2.0, because it contains derivative works of the openapi-generator
project (See the associated license notices in the com.twardyece.dmtf.openapi
package). The Rust crates are dual-licensed under MIT or Apache-2.0.
