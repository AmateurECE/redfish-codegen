use zip::ZipArchive;

use crate::{batch::Process, stages::Specification};
use std::{fs::File, path::PathBuf};

/// These archives are released by the DMTF and the SNIA. Probably, they are downloaded from the
/// internet to the build machine.
pub struct ReleasedArchives {
    pub redfish_schemas: PathBuf,
    pub registries: PathBuf,
    pub swordfish_schemas: PathBuf,
}

pub struct ExtractArchives {
    archives: ReleasedArchives,
}

impl ExtractArchives {
    pub fn new(archives: ReleasedArchives) -> Self {
        Self { archives }
    }
}

impl Process<()> for ExtractArchives {
    type Output = Specification;
    fn process(self, _: ()) -> Self::Output {
        let file = File::open(self.archives.redfish_schemas).unwrap();
        let _archive = ZipArchive::new(file).unwrap();
        todo!()
    }
}
