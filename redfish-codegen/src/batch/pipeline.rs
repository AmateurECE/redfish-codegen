use super::{private, Process, Stage};

pub struct Pipeline<Proc, PreviousStage> {
    pub(super) process: Proc,
    pub(super) previous: PreviousStage,
}

pub struct PipelineBuilder;
impl private::Sealed for PipelineBuilder {}
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

impl<P, S> private::Sealed for Pipeline<P, S> {}
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
