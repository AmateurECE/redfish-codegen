# `redfish-macros`

This crate provides some procedural macros which are useful in the
implementation of Redfish services.

Currently, the only macro present in this repository is the macro
`IntoRedfishMessage`, which provides an implementation of
`Into<redfish_codegen::models::message::<version>::Message>`. This is helpful,
for example, for Redfish implementors who choose to create their own
registries. It requires one attribute on an `enum`, and four attributes on each
variant, as shown below:

```
/// This registry defines the base messages for Redfish
#[derive(Clone, Debug, IntoRedfishMessage)]
#[message(crate::models::message::v1_1_2::Message)]
pub enum Base {
    /// This message shall be used to indicate that a property was not updated due to an internal service error, but the service is still functional.
    #[message(message = "The property %1 was not updated due to an internal service error.  The service is still operational.")]
    #[message(id = "Base.1.15.0.PropertyNotUpdated")]
    #[message(severity = "crate::models::resource::Health::Critical")]
    #[message(resolution = "Resubmit the request.  If the problem persists, check for additional messages and consider resetting the service.")]
    PropertyNotUpdated(
        /// This argument shall contain the name of the property.
        String,
    ),

    ...
```

It is then possible to do the following:

```
let base = Base::PropertyNotUpdated("a_property");
let message: Message = base.into();
```
