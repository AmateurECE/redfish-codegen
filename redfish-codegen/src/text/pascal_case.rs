use fancy_regex::Regex;
use lazy_static::lazy_static;

use super::{
    word::{IntoWords, Word, SPECIAL_ABBREVIATIONS},
    CaseConversionError,
};

lazy_static! {
    static ref PASCAL_CASE: Regex =
        Regex::new("([A-Z][a-z]+)|([A-Z]+)(?=[A-Z][a-z])|([A-Z0-9]+)").unwrap();
}

/// A name in PascalCase.
#[derive(Debug, Eq, PartialEq, Hash)]
pub struct PascalCaseName(Vec<Word>);
impl PascalCaseName {
    pub fn new(name: String) -> Result<Self, CaseConversionError> {
        // PascalCase is a little harder than other cases. Since PascalCase
        // strings may contain substrings that are not in PascalCase, e.g.
        // abbreviations like "PCIe", we have to intentionally handle those
        // before attempting to parse the identifier(s) as PascalCase.
        let mut identifiers = Vec::new();
        identifiers.push(name.clone());
        let mut discovered_length = 0;
        while discovered_length < name.len() {
            let identifier = identifiers.get(identifiers.len() - 1).unwrap();
            let mut index_of_largest_abbreviation: Option<usize> = None;
            let mut length_of_largest_abbreviation = 0;
            SPECIAL_ABBREVIATIONS.keys().for_each(|abbreviation| {
                let index = identifier.find(abbreviation);
                if index.is_some() && abbreviation.len() > length_of_largest_abbreviation {
                    index_of_largest_abbreviation = index;
                    length_of_largest_abbreviation = abbreviation.len();
                }
            });

            if index_of_largest_abbreviation.is_none() {
                // This identifier does not contain an abbreviation.
                discovered_length += identifier.len();
                continue;
            }

            let abbreviation = SPECIAL_ABBREVIATIONS
                .keys()
                .nth(index_of_largest_abbreviation.unwrap())
                .unwrap();
            discovered_length += abbreviation.len();

            // We may run into a case where the current identifier is equal to
            // the current abbreviation. There's nothing we must do in that
            // scenario.
            if abbreviation == identifier {
                continue;
            }

            if let Some(index) = identifier.find(abbreviation) {
                // Three cases:
                //   1. It's at the beginning of the string (result is two strings)
                if 0 == index {
                    let first = identifier[..abbreviation.len()].to_owned();
                    let second = identifier[abbreviation.len()..].to_owned();
                    *identifiers.last_mut().unwrap() = first.to_string();
                    identifiers.push(second.to_owned());
                }
                //   2. It's at the end of the string (result is two strings)
                else if index + abbreviation.len() == identifier.len() {
                    let first = identifier[..index].to_owned();
                    let second = identifier[index..].to_owned();
                    *identifiers.last_mut().unwrap() = first.to_string();
                    identifiers.push(second.to_owned());
                }
                //   3. It's in the middle of the string (result is three strings)
                else {
                    let first = identifier[..index].to_owned();
                    let second = identifier[index..index + abbreviation.len()].to_owned();
                    let third = identifier[index + abbreviation.len()..].to_owned();
                    *identifiers.last_mut().unwrap() = first.to_string();
                    identifiers.push(second.to_owned());
                    identifiers.push(third.to_owned());
                }
            }
        }

        if let Some(identifier) = identifiers.get(0) {
            if !SPECIAL_ABBREVIATIONS.contains_key(identifier.as_str())
                && !char::is_uppercase(identifier.chars().nth(0).unwrap())
            {
                return Err(CaseConversionError::new("PascalCase".to_string(), name));
            }
        }

        let mut pascal_case_name = PascalCaseName(Vec::new());
        for identifier in identifiers.iter() {
            // Each identifier is either a special abbreviation, or a PascalCased string.
            if SPECIAL_ABBREVIATIONS.contains_key(identifier.as_str()) {
                let word = SPECIAL_ABBREVIATIONS.get(identifier.as_str()).unwrap();
                pascal_case_name.0.push(word.clone());
            } else {
                pascal_case_name.parse_pascal_case_name(identifier)?;
            }
        }

        Ok(pascal_case_name)
    }

    fn parse_pascal_case_name(&mut self, name: &str) -> Result<(), CaseConversionError> {
        for capture in PASCAL_CASE.captures_iter(name) {
            if let Err(e) = capture {
                return Err(CaseConversionError::new(
                    "PascalCase".to_string(),
                    e.to_string(),
                ));
            }

            let capture = capture.unwrap();
            if let Some(group) = capture.get(1) {
                self.0.push(Word::Word(group.as_str().to_string()));
            } else if let Some(group) = capture.get(2) {
                self.0.push(Word::Abbreviation(group.as_str().to_string()));
            } else if let Some(group) = capture.get(3) {
                self.0.push(Word::Abbreviation(group.as_str().to_string()));
            } else {
                return Err(CaseConversionError::new(
                    "PascalCase".to_string(),
                    name.to_string(),
                ));
            }
        }

        if self.0.is_empty() && !name.is_empty() {
            return Err(CaseConversionError::new(
                "PascalCase".to_string(),
                name.to_string(),
            ));
        }

        Ok(())
    }
}

impl IntoWords for PascalCaseName {
    type IntoIter = std::vec::IntoIter<Word>;

    fn into_words(self) -> Self::IntoIter {
        self.0.into_iter()
    }
}

impl ToString for PascalCaseName {
    fn to_string(&self) -> String {
        self.0
            .iter()
            .map(|w| w.clone().into_capitalized())
            .collect::<String>()
    }
}

#[cfg(test)]
mod test {
    use super::PascalCaseName;

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
            let parsed = PascalCaseName::new(name.to_string());
            assert!(parsed.is_ok());
            assert_eq!(*name, &parsed.unwrap().to_string());
        }
    }
}
