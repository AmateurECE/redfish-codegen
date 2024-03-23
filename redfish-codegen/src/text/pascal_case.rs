use fancy_regex::Regex;
use lazy_static::lazy_static;

use super::{
    lexer::{Lexer, Token},
    word::{FromWords, IntoWords, ToWords, Word},
    CaseConversionError,
};

lazy_static! {
    // TODO: This regex uses look-around groups, which are not supported by
    // the standard regex crate. Refactor this class not to use regex so that
    // we can drop the dependency.
    static ref PASCAL_CASE: Regex =
        Regex::new("([A-Z][a-z]+)|([A-Z]+)(?=[A-Z][a-z])|([A-Z0-9]+)").unwrap();
}

/// A name in PascalCase.
#[derive(Clone, Debug, Eq, PartialEq, Hash)]
pub struct PascalCaseName(Vec<Word>);
impl PascalCaseName {
    /// Parse a name in PascalCase to produce a [PascalCaseName].
    pub fn parse(name: String) -> Result<Self, CaseConversionError> {
        // NOTE: This check is required in order to "fail" camelCase identifiers
        let uppercase_first_letter = name
            .chars()
            .nth(0)
            .map_or_else(|| false, char::is_uppercase);
        if !uppercase_first_letter {
            return Err(CaseConversionError::new("PascalCase".to_string(), name));
        }

        let lexer = Lexer::new();
        let identifiers = lexer.analyze(name.clone());

        let mut words = Vec::new();
        for token in identifiers.into_iter() {
            match token {
                Token::Regular(name) => parse_pascal_case_name(&mut words, name)?,
                Token::Irregular(word) => words.push(word),
            }
        }

        Ok(Self(words))
    }
}

impl ToWords for PascalCaseName {
    type Iter<'a> = std::slice::Iter<'a, Word>;

    fn to_words(&self) -> Self::Iter<'_> {
        self.0.iter()
    }
}

impl IntoWords for PascalCaseName {
    type IntoIter = std::vec::IntoIter<Word>;

    fn into_words(self) -> Self::IntoIter {
        self.0.into_iter()
    }
}

impl FromWords for PascalCaseName {
    fn from_words<W>(words: W) -> Self
    where
        W: IntoWords,
    {
        Self(words.into_words().collect::<Vec<Word>>())
    }
}

impl ToString for PascalCaseName {
    fn to_string(&self) -> String {
        self.0
            .iter()
            .map(|w| w.to_capitalized())
            .collect::<String>()
    }
}

fn parse_pascal_case_name(words: &mut Vec<Word>, name: String) -> Result<(), CaseConversionError> {
    for capture in PASCAL_CASE.captures_iter(&name) {
        if capture.is_err() {
            return Err(CaseConversionError::new("PascalCase".to_string(), name));
        }

        let capture = capture.unwrap();
        if let Some(group) = capture.get(1) {
            words.push(Word::Word(group.as_str().to_string()));
        } else if let Some(group) = capture.get(2) {
            words.push(Word::Abbreviation(group.as_str().to_string()));
        } else if let Some(group) = capture.get(3) {
            words.push(Word::Abbreviation(group.as_str().to_string()));
        } else {
            return Err(CaseConversionError::new("PascalCase".to_string(), name));
        }
    }

    if words.is_empty() && !name.is_empty() {
        return Err(CaseConversionError::new("PascalCase".to_string(), name));
    }

    Ok(())
}

#[cfg(test)]
mod test {
    use super::PascalCaseName;

    /// Test that parsing a mix of valid PascalCase identifiers succeeds.
    #[test]
    fn pascal_case_parsing() {
        // This mix catches any obvious error with the parsing of PascalCase
        // names.
        let names = [
            "Systems",
            "Vec",
            "AccountService",
            "USBControllerCollection",
            "VLanNetworkInterface",
        ];

        for name in names.iter() {
            let parsed = PascalCaseName::parse(name.to_string());
            assert!(parsed.is_ok());
            assert_eq!(*name, &parsed.unwrap().to_string());
        }
    }

    /// Test that parsing camelCase identifiers fails
    #[test]
    fn camel_case_fails() {
        let parsed = PascalCaseName::parse("camelCaseName".to_string());
        assert!(parsed.is_err());
    }

    /// Test that parsing snake_case identifiers fails
    #[test]
    fn snake_case_fails() {
        let parsed = PascalCaseName::parse("snake_case_name".to_string());
        assert!(parsed.is_err());
    }
}
