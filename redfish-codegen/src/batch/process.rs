/// A [Process] is a function from an input to an output type that consumes `Self`. A process is
/// invoked exactly once in its lifetime. Since it's only called once, it must produce its full
/// output and is free to fully consume its input.
pub trait Process<Input> {
    /// The type of value produced by this process.
    type Output;
    fn process(self, input: Input) -> Self::Output;
}

impl<F, In, Out> Process<In> for F
where
    F: FnOnce(In) -> Out,
{
    type Output = Out;
    fn process(self, input: In) -> Out {
        self(input)
    }
}
