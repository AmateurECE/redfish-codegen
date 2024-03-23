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
    pub fn to_uppercase(&self) -> String {
        match self {
            Word::Word(value) => value.to_uppercase(),
            Word::Abbreviation(value) => value.clone(),
        }
    }

    pub fn to_capitalized(&self) -> String {
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
            Word::Abbreviation(value) => value.clone(),
        }
    }

    pub fn to_lowercase(&self) -> String {
        match self {
            Word::Word(value) => value.to_lowercase(),
            Word::Abbreviation(value) => value.to_lowercase(),
        }
    }

    /// Access the word as a [str][std::str]. This method makes no guarantee
    /// about the case of the result.
    pub fn as_str(&self) -> &str {
        match self {
            Word::Word(ref value) => value,
            Word::Abbreviation(ref value) => value,
        }
    }
}

/// Represents a phrase that consumers can break down into words and iterate
/// over.
pub trait ToWords {
    /// The type of the word iterator.
    type Iter<'a>: Iterator<Item = &'a Word>
    where
        Self: 'a;

    /// Consume the phrase, breaking it down into words that can be iterated
    /// over.
    fn to_words(&self) -> Self::Iter<'_>;
}

/// Similar to [ToWords], but consumes the underlying object, yielding its
/// words.
pub trait IntoWords {
    type IntoIter: Iterator<Item = Word>;

    fn into_words(self) -> Self::IntoIter;
}

/// Allows types to instantiate themselves from a type that implements
/// [IntoWords]
pub trait FromWords {
    fn from_words<W>(words: W) -> Self
    where
        W: IntoWords;
}
