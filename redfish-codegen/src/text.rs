use std::{error::Error, fmt::Display};

mod camel_case;
mod lexer;
mod pascal_case;
mod snake_case;
mod word;

/// An error that results when it's not possible to convert a phrase to a
/// particular case.
#[derive(Debug)]
pub struct CaseConversionError {
    /// The phrase for which conversion was attempted.
    text: String,

    /// The target case of the attempted conversion.
    target_case: String,
}

impl CaseConversionError {
    pub fn new(text: String, target_case: String) -> CaseConversionError {
        Self { text, target_case }
    }
}

impl Display for CaseConversionError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "String {} is not convertible to {}",
            self.text, self.target_case
        )
    }
}

impl Error for CaseConversionError {}
