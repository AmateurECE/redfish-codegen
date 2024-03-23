use super::{
    pascal_case::PascalCaseName,
    word::{IntoWords, ToWords, Word},
    CaseConversionError,
};
use fancy_regex::Regex;

lazy_static::lazy_static! {
    static ref PREFIX: Regex = Regex::new("^[a-z0-9]+").unwrap();
}

/// A name in camelCase.
#[derive(Clone, Debug, Eq, Hash, PartialEq)]
pub struct CamelCaseName(Vec<Word>);
impl CamelCaseName {
    /// Parse an identifier in camelCase to produce a [CamelCaseName].
    pub fn parse(name: String) -> Result<Self, CaseConversionError> {
        let error = || CaseConversionError::new("camelCase".to_string(), name.to_string());
        let prefix = PREFIX
            .find(&name)
            .map_err(|_| error())?
            .ok_or_else(error)?
            .as_str();
        let mut words = vec![Word::Word(prefix.to_string())];
        if prefix != name {
            let suffix = PascalCaseName::parse(name.strip_prefix(prefix).unwrap().to_string())?;
            words.extend(suffix.into_words());
        }
        Ok(Self(words))
    }
}

impl ToWords for CamelCaseName {
    type Iter<'a> = std::slice::Iter<'a, Word>;

    fn to_words(&self) -> Self::Iter<'_> {
        self.0.iter()
    }
}

impl IntoWords for CamelCaseName {
    type IntoIter = std::vec::IntoIter<Word>;

    fn into_words(self) -> Self::IntoIter {
        self.0.into_iter()
    }
}

impl ToString for CamelCaseName {
    fn to_string(&self) -> String {
        let mut iter = self.to_words();
        iter.next().map(|w| w.to_lowercase()).unwrap_or_default()
            + &iter.map(|w| w.to_capitalized()).collect::<String>()
    }
}

#[cfg(test)]
mod test {
    use super::CamelCaseName;

    #[test]
    fn basic_parsing() {
        let names = ["additionalProperties", "metadata"];
        for name in names.iter() {
            let result = CamelCaseName::parse(name.to_string());
            assert!(result.is_ok());
            assert_eq!(name, &result.unwrap().to_string());
        }
    }

    #[test]
    fn pascal_case_fails() {
        let result = CamelCaseName::parse("PCIeDeviceCollection".to_string());
        assert!(result.is_err());
    }

    #[test]
    fn snake_case_fails() {
        let result = CamelCaseName::parse("odata_v4".to_string());
        assert!(result.is_err());
    }
}
