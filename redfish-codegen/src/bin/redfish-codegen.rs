use redfish_codegen::{
    batch::{Execute, Pipeline, Stage},
    extract::{ExtractArchives, ReleasedArchives},
    generate::GenerateModels,
};

fn main() {
    let archives = ReleasedArchives {
        redfish_schemas: todo!(),
        registries: todo!(),
        swordfish_schemas: todo!(),
    };
    let pipeline = Pipeline::builder()
        .stage(ExtractArchives::new(archives))
        .stage(GenerateModels::new("".into()));
    pipeline.execute();
}
