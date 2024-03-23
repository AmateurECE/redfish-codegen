//! The lexer breaks down identifiers into tokens that follow the canonical
//! rules of a target case. The problem with case conversion is that some
//! lexical elements just don't follow the standard rules for a particular
//! case. For example, how does one interpret "iSCSI" as a camelCase
//! identifier? It's valid, syntactically, to interpret it as two words:
//! "i", and "SCSI". However, we know that iSCSI is a proper noun, and so
//! it should be excluded from being parsed as a canonical camelCase
//! identifier. The lexer's job is to look for these nouns in an identifier
//! and inform parsers about them.

use super::word::Word;
use lazy_static::lazy_static;
use std::collections::HashMap;

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

/// A Token is a lexical element that has associated rules for parsing based
/// on a target case.
#[derive(Debug, Eq, PartialEq, Hash)]
pub enum Token {
    /// A lexical element that either follows canonical parsing rules of the
    /// target case, or is unknown to the lexer.
    Regular(String),
    /// A lexical element that has special parsing rules.
    Irregular(Word),
}

pub struct Lexer;

struct TokenMatch {
    pub abbreviation: &'static str,
    pub length: usize,
}

impl Lexer {
    pub fn new() -> Self {
        Self
    }

    pub fn analyze(&self, name: String) -> Vec<Token> {
        let mut identifiers = vec![Token::Regular(name.clone())];
        let mut discovered_length = 0;
        while discovered_length < name.len() {
            let identifier = match identifiers.last().unwrap() {
                Token::Regular(name) => name,
                Token::Irregular(ref abbreviation) => {
                    discovered_length += abbreviation.as_str().len();
                    continue;
                }
            };

            let lexeme = match find_largest_abbreviation(identifier) {
                Some(lexeme) => lexeme,
                None => {
                    // This identifier does not contain an abbreviation.
                    discovered_length += identifier.len();
                    continue;
                }
            };

            discovered_length += lexeme.abbreviation.len();

            let mut new_tokens = split_token(identifier, lexeme.abbreviation);
            println!("{:?}", identifiers);
            identifiers.pop();
            identifiers.append(&mut new_tokens);
            println!("{:?}", identifiers);
        }

        identifiers
    }
}

/// Find the largest known abbreviation in the string
fn find_largest_abbreviation(name: &str) -> Option<TokenMatch> {
    let mut result: Option<TokenMatch> = None;
    SPECIAL_ABBREVIATIONS.keys().for_each(|abbreviation| {
        if name.contains(abbreviation) {
            let replace = match &result {
                Some(lexeme) => abbreviation.len() > lexeme.length,
                None => true,
            };

            if replace {
                result = Some(TokenMatch {
                    abbreviation,
                    length: abbreviation.len(),
                })
            }
        }
    });

    result
}

fn split_token(identifier: &str, abbreviation: &str) -> Vec<Token> {
    let irregular = Token::Irregular(SPECIAL_ABBREVIATIONS.get(abbreviation).unwrap().clone());

    // If the current identifier is equal to the abbreviation, we
    // place a single irregular token for it.
    if abbreviation == identifier {
        vec![irregular]
    }
    // Otherwise, the abbreviation is a substring, so we need to do some
    // combination of splitting to obtain it.
    else if let Some(index) = identifier.find(abbreviation) {
        // Three cases:
        //   1. It's at the beginning of the string (result is two strings)
        if 0 == index {
            vec![
                irregular,
                Token::Regular(identifier[abbreviation.len()..].to_owned()),
            ]
        }
        //   2. It's at the end of the string (result is two strings)
        else if index + abbreviation.len() == identifier.len() {
            vec![Token::Regular(identifier[..index].to_owned()), irregular]
        }
        //   3. It's in the middle of the string (result is three strings)
        else {
            vec![
                Token::Regular(identifier[..index].to_owned()),
                irregular,
                Token::Regular(identifier[index + abbreviation.len()..].to_owned()),
            ]
        }
    } else {
        Vec::new()
    }
}

#[cfg(test)]
mod test {
    use super::Word;
    use super::{Lexer, Token};

    #[test]
    fn basic_split() {
        let strings = [
            (
                "PascalCase".to_string(),
                vec![Token::Regular("PascalCase".to_string())],
            ),
            (
                "NVMeoF".to_string(),
                vec![
                    Token::Irregular(Word::Abbreviation("NVMe".to_string())),
                    Token::Irregular(Word::Abbreviation("oF".to_string())),
                ],
            ),
            (
                "VLanNetworkInterface".to_string(),
                vec![
                    Token::Irregular(Word::Abbreviation("VLan".to_string())),
                    Token::Regular("NetworkInterface".to_string()),
                ],
            ),
        ];

        let lexer = Lexer::new();
        for (string, tokens) in strings.into_iter() {
            let result = lexer.analyze(string);
            assert_eq!(result, *tokens);
        }
    }
}
