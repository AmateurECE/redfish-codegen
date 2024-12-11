//! "Wiring" types that reify concepts from the Batch-sequential architecture pattern.

pub trait Process<Input> {
    type Output;
    fn process(self, input: Input) -> Self::Output;
}

pub trait Stage<P, In, Out> {
    type Result<R>;
    fn stage<Q>(self, process: Q) -> Self::Result<Q>
    where
        Q: Process<Out>;
}

pub struct Pipeline<Proc, PreviousStage> {
    process: Proc,
    previous: PreviousStage,
}

pub struct PipelineBuilder;
impl Stage<(), (), ()> for PipelineBuilder {
    type Result<R> = Pipeline<R, ()>;
    fn stage<Q>(self, process: Q) -> Self::Result<Q>
    where
        Q: Process<()>,
    {
        Pipeline {
            process,
            previous: (),
        }
    }
}

impl Pipeline<(), ()> {
    pub fn builder() -> PipelineBuilder {
        PipelineBuilder
    }
}

impl<P, S, Input, Output> Stage<P, Input, Output> for Pipeline<P, S>
where
    P: Process<Input, Output = Output>,
{
    type Result<R> = Pipeline<R, Self>;
    fn stage<Q>(self, process: Q) -> Self::Result<Q>
    where
        Q: Process<Output>,
    {
        Pipeline {
            process,
            previous: self,
        }
    }
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

trait RunStage {
    type Output;
    fn run_stage(self) -> Self::Output;
}

impl RunStage for () {
    type Output = ();
    fn run_stage(self) -> Self::Output {
        ()
    }
}

impl<P, R> RunStage for Pipeline<P, R>
where
    P: Process<<R as RunStage>::Output>,
    R: RunStage,
{
    type Output = P::Output;
    fn run_stage(self) -> Self::Output {
        let Self { process, previous } = self;
        process.process(previous.run_stage())
    }
}

pub trait Execute<In> {
    fn execute(self);
}

impl<P, In, N> Execute<In> for Pipeline<P, N>
where
    P: Process<In, Output = ()>,
    Self: RunStage,
{
    fn execute(self) {
        self.run_stage();
    }
}
