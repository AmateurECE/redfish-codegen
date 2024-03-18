# `redfish-models`

An unopinionated translation of the [Redfish][1] Schema Bundle (DSP8010) and
the Base Registries Specification (DSP8011) into Rust. Models are translated
to structs and enumerations, and API endpoints are translated into traits.

There is no logic in this crate. This is only a translation of the contract
described in the specification documents into Rust.

[1]: https://www.dmtf.org/standards/redfish
