use std::io;
use std::process::Command;

fn main() -> io::Result<()> {
    println!("cargo:rerun-if-changed=build.rs");
    println!("cargo:rerun-if-changed=../generate.mk");
    println!("cargo:rerun-if-changed=../redfish-generator");
    Command::new("make")
        .args(["-C", "..", "-f", "generate.mk", "models"])
        .status()
        .map(|_| ())
}
