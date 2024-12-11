use clap::Parser;
use redfish_codegen::{
    batch::{Execute, Pipeline, Stage},
    extract::{ExtractArchives, ReleasedArchives},
    generate::GenerateModels,
};
use std::path::PathBuf;

const JAR_FILE_PATH: &str = "redfish-generator/target/redfish-codegen-0.3.1-SNAPSHOT.jar";

#[derive(Parser)]
struct Args {
    /// Path to a directory containing DMTF/SNIA specification archive files.
    directory: PathBuf,
}

fn main() {
    let args = Args::parse();
    let directory = args.directory.as_path();
    let archives = ReleasedArchives {
        redfish_schemas: directory.join("DSP8010_2024.2.zip"),
        registries: directory.join("DSP8011_2024.1.zip"),
        swordfish_schemas: directory.join("Swordfish_v1.2.6_Schema.zip"),
    };

    let pipeline = Pipeline::builder()
        .stage(ExtractArchives::new(archives))
        .stage(GenerateModels::new(JAR_FILE_PATH.into()));
    pipeline.execute();
}
