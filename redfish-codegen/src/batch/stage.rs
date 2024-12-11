use super::{private, Process};

/// A [Stage] is a segment in a pipeline. A stage executes a [Process] and can be linked to exactly
/// one or two other stages. This trait is sealed, because it is not intended to be implemented by
/// consumers.
pub trait Stage<P, In, Out>: private::Sealed {
    type Result<R>;
    fn stage<Q>(self, process: Q) -> Self::Result<Q>
    where
        Q: Process<Out>;
}
