use crate::{batch::Process, stages::Specification};
use std::path::PathBuf;

/// The generation mode
pub enum Mode {
    /// Generate Client models
    Client,
    /// Generate Service models
    Service,
}

pub struct GenerateModels {
    mode: Mode,
    jar_file: PathBuf,
}

impl GenerateModels {
    pub fn new(jar_file: PathBuf) -> Self {
        Self::with_mode(jar_file, Mode::Service)
    }

    pub fn with_mode(jar_file: PathBuf, mode: Mode) -> Self {
        Self { jar_file, mode }
    }
}

impl Process<Specification> for GenerateModels {
    type Output = ();
    fn process(self, input: Specification) -> Self::Output {
        todo!()
    }
}
