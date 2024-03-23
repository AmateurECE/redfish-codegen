/// A Word can be one of two things.
/// 1. A single uninterruptible sentence elment, which is treated in the
///    usual way.
/// 2. An abbreviation, which has special rules around capitalization.
/// Capitalization and uppercase conversion have no semantic meaning for an
/// abbreviation.
#[derive(Clone, Debug, Eq, PartialEq, Hash)]
pub enum Word {
    Word(String),
    Abbreviation(String),
}

impl Word {
    pub fn into_upper_case(self) -> String {
        match self {
            Word::Word(value) => value.to_uppercase(),
            Word::Abbreviation(value) => value,
        }
    }

    pub fn into_capitalized(self) -> String {
        match self {
            Word::Word(value) => {
                let mut iter = value.chars();
                let first = iter
                    .next()
                    .map(|s| s.to_uppercase().to_string())
                    .unwrap_or(String::new());
                let rest = iter.collect::<String>().to_lowercase();
                first + &rest
            }
            Word::Abbreviation(value) => value,
        }
    }

    pub fn into_lower_case(self) -> String {
        match self {
            Word::Word(value) => value.to_lowercase(),
            Word::Abbreviation(value) => value.to_lowercase(),
        }
    }
}

/// Represents a phrase that consumers can break down into words and iterate
/// over.
pub trait IntoWords {
    /// The type of the word iterator.
    type IntoIter: Iterator<Item = Word>;

    /// Consume the phrase, breaking it down into words that can be iterated
    /// over.
    fn into_words(self) -> Self::IntoIter;
}
