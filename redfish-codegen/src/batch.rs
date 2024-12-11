//! Wiring types that reify concepts from the Batch-sequential architecture pattern.
//!
//! A pipeline produces a batch of data in one stage, transforms it through subsequent stages, and
//! consumes it through its final stage. Use [Pipeline::builder] to construct a [Pipeline], and
//! execute it using [Execute].
//!
//! ```
//! fn make_message(input: ()) -> String {
//!     "Hello, world!".to_string()
//! }
//!
//! fn print_message(input: String) {
//!     println!("{}", &input);
//! }
//!
//! let pipeline = Pipeline::builder()
//!     .stage(make_message)
//!     .stage(print_message);
//! pipeline.execute();
//! ```
//!
//! Stages can be constructed using bare functions, or types that implement [Process].
//!
//! ```
//! struct Hello;
//! impl Process<()> for Hello {
//!     type Output = String;
//!     fn process(self, input: ()) -> Self::Output {
//!         "Hello, world!".to_string()
//!     }
//! }
//!
//! # fn print_message(input: String) {
//! #     println!("{}", &input);
//! # }
//! #
//! Pipeline::builder()
//!     .stage(Hello)
//!     .stage(print_message)
//!     .execute();
//! ```
//!
//! Signatures of [Process]es are type-checked, so that it's only possible to connect two stages
//! together if the output of the previous stage is compatible with the input of the next stage.
//! Likewise, it's not possible to execute a pipeline until a final stage that produces no output
//! value is connected.

mod process;
pub use execute::*;

mod execute;
pub use process::*;

mod stage;
pub use stage::*;

mod pipeline;
pub use pipeline::*;

mod private {
    pub trait Sealed {}
}
