use fancy_regex::Regex;

use super::{
    word::{FromWords, IntoWords, ToWords, Word},
    CaseConversionError,
};

lazy_static::lazy_static! {
    static ref SNAKE_CASE: Regex = Regex::new("([a-z0-9]+)").unwrap();
}

/// A name in snake_case.
#[derive(Clone, Debug, Eq, PartialEq, Hash)]
pub struct SnakeCaseName(Vec<Word>);
impl SnakeCaseName {
    /// Parse an identifier in snake_case to produce a [SnakeCaseName].
    pub fn parse(name: String) -> Result<Self, CaseConversionError> {
        if name.find(char::is_uppercase).is_some() {
            return Err(CaseConversionError::new("SnakeCaseName".to_string(), name));
        }

        let mut words = Vec::new();
        for capture in SNAKE_CASE.captures_iter(&name) {
            if let Ok(capture) = capture {
                if let Some(group) = capture.get(1) {
                    words.push(Word::Word(group.as_str().to_string()));
                }
            } else {
                return Err(CaseConversionError::new("SnakeCaseName".to_string(), name));
            }
        }

        Ok(Self(words))
    }

    pub fn to_uppercase(&self) -> String {
        self.to_string().to_uppercase()
    }
}

impl ToWords for SnakeCaseName {
    type Iter<'a> = std::slice::Iter<'a, Word>;

    fn to_words(&self) -> Self::Iter<'_> {
        self.0.iter()
    }
}

impl IntoWords for SnakeCaseName {
    type IntoIter = std::vec::IntoIter<Word>;

    fn into_words(self) -> Self::IntoIter {
        self.0.into_iter()
    }
}

impl FromWords for SnakeCaseName {
    fn from_words<W>(words: W) -> Self
    where
        W: IntoWords,
    {
        Self(words.into_words().collect::<Vec<Word>>())
    }
}

impl ToString for SnakeCaseName {
    fn to_string(&self) -> String {
        self.to_words()
            .map(|w| w.to_lowercase())
            .collect::<Vec<String>>()
            .join("_")
    }
}

#[cfg(test)]
mod test {
    use super::SnakeCaseName;

    #[test]
    fn basic_parsing() {
        let names = ["redfish_codegen", "odata_v4", "metadata", "i64"];

        for name in names {
            let result = SnakeCaseName::parse(name.to_string());
            assert!(result.is_ok());
            assert_eq!(name, &result.unwrap().to_string());
        }
    }

    #[test]
    fn upper_snake_case() {
        let result = SnakeCaseName::parse("odata_v4".to_string());
        assert!(result.is_ok());
        assert_eq!("ODATA_V4", result.unwrap().to_uppercase());
    }

    #[test]
    fn pascal_case_fail() {
        let result = SnakeCaseName::parse("PCIeFunction".to_string());
        assert!(result.is_err());
    }

    #[test]
    fn camel_case_fail() {
        let result = SnakeCaseName::parse("additionalProperties".to_string());
        assert!(result.is_err());
    }
}
