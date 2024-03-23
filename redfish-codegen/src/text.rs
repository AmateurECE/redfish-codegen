use std::{error::Error, fmt::Display};

use self::{
    camel_case::CamelCaseName, pascal_case::PascalCaseName, snake_case::SnakeCaseName,
    word::FromWords,
};

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

pub fn to_pascal_case(name: String) -> Result<PascalCaseName, CaseConversionError> {
    PascalCaseName::parse(name.clone())
        .or_else(|_| SnakeCaseName::parse(name.clone()).map(PascalCaseName::from_words))
        .or_else(|_| CamelCaseName::parse(name).map(PascalCaseName::from_words))
}

pub fn to_snake_case(name: String) -> Result<SnakeCaseName, CaseConversionError> {
    SnakeCaseName::parse(name.clone())
        .or_else(|_| PascalCaseName::parse(name.clone()).map(SnakeCaseName::from_words))
        .or_else(|_| CamelCaseName::parse(name.clone()).map(SnakeCaseName::from_words))
}

#[cfg(test)]
mod test {
    use crate::text::{to_pascal_case, to_snake_case};

    use super::CaseConversionError;

    #[test]
    fn snake_case_to_pascal_case() -> Result<(), CaseConversionError> {
        assert_eq!(
            "PcieFunctionCollection",
            &to_pascal_case("pcie_function_collection".to_string())?.to_string()
        );
        Ok(())
    }

    #[test]
    fn camel_case_to_pascal_case() -> Result<(), CaseConversionError> {
        assert_eq!(
            "AdditionalProperties",
            &to_pascal_case("additionalProperties".to_string())?.to_string()
        );
        Ok(())
    }

    #[test]
    fn pascal_case_to_snake_case() -> Result<(), CaseConversionError> {
        assert_eq!(
            "pcie_function_collection",
            &to_snake_case("PCIeFunctionCollection".to_string())?.to_string()
        );
        Ok(())
    }

    #[test]
    fn camel_case_to_snake_case() -> Result<(), CaseConversionError> {
        assert_eq!(
            "additional_properties",
            &to_snake_case("additionalProperties".to_string())?.to_string()
        );
        Ok(())
    }
}
