use super::{private, Pipeline, Process};

// Helper to execute intermediate stages.
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

/// Enables clients to execute a [Pipeline]. For a pipeline to be executable, it must be
/// "complete"--it's final stage must generate no output.
pub trait Execute<In>: private::Sealed {
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
