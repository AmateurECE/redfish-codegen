use lazy_static::lazy_static;
use std::collections::HashMap;

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

lazy_static! {
    /// List of known special abbreviations.
    pub static ref SPECIAL_ABBREVIATIONS: HashMap<&'static str, Word> = {
        let mut m = HashMap::new();
        m.insert("PCIe", Word::Abbreviation("PCIe".to_string()));
        m.insert("VLan", Word::Abbreviation("VLan".to_string()));
        m.insert("VLANs", Word::Abbreviation("VLANs".to_string()));
        m.insert("mUSB", Word::Word("mUSB".to_string()));
        m.insert("uUSB", Word::Word("uUSB".to_string()));
        m.insert("cSFP", Word::Word("cSFP".to_string()));
        m.insert("IPv4", Word::Abbreviation("IPv4".to_string()));
        m.insert("IPv6", Word::Abbreviation("IPv6".to_string()));
        m.insert("kWh", Word::Abbreviation("kWh".to_string()));
        m.insert("iSCSI", Word::Word("iSCSI".to_string()));
        m.insert("NVMe", Word::Abbreviation("NVMe".to_string()));
        m.insert("oF", Word::Abbreviation("oF".to_string()));
        m.insert("OAuth2", Word::Abbreviation("OAuth2".to_string()));
        m.insert("OAuth", Word::Abbreviation("OAuth".to_string()));
        m
    };
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
