use regex::Regex;
use std::collections::{HashMap, HashSet};

use crate::reserved::IDENTIFIERS;
use crate::transform;

/*
 * State shared by symbol collection and transformation.
 *
 * Project obfuscation reuses one context for every file, ensuring a symbol
 * receives the same replacement wherever it appears.
 */
pub struct ObfuscationContext {
    pub(crate) symbols: HashMap<String, String>,
    pub(crate) blacklist: HashSet<String>,
    separate: bool,
    seed: Option<u64>,
    global: usize,
    variables: usize,
    functions: usize,
}

impl ObfuscationContext {
    pub fn new(excluded: &[String], separate: bool, seed: Option<u64>) -> Self {
        Self {
            symbols: HashMap::new(),
            blacklist: excluded.iter().cloned().collect(),
            separate,
            seed,
            global: 0,
            variables: 0,
            functions: 0,
        }
    }

    /* Scans declarations and interface symbols before rewriting source. */
    pub fn collect(&mut self, source: &str) {
        let declaration = Regex::new(r"\b(?:(?:(?:lowp|mediump|highp|precision|const)\s+)*)(?:void|bool|int|uint|float|double|vec[234]|u?ivec[234]|bvec[234]|dvec[234]|mat[234](?:x[234])?|sampler[123]D(?:Shadow)?|samplerCube(?:Shadow)?)\s+([\w\s,\[\]=().+/*-]+?)(?:[;{])").unwrap();
        let io = Regex::new(r"\b(?:(?:attribute|const|uniform|varying|in|out|inout|centroid|flat|smooth|noperspective|invariant)|layout\s*\([^)]*\)\s*(?:in|out)?)\s+(?:\w+\s+)*(\w+)\s+([\w\s,\[\]]+);").unwrap();
        let keep = Regex::new(r"(?://|/\*)\s*@keep\s+([\w\s,]+)").unwrap();
        let identifier = Regex::new(r"\b([a-zA-Z_][a-zA-Z0-9_]*)\b").unwrap();
        let line_comment = Regex::new(r"//.*").unwrap();
        let block_comment = Regex::new(r"/\*.*?\*/").unwrap();
        let function = Regex::new(r"^([a-zA-Z_]\w*)\s*\(").unwrap();

        for original in source.lines() {
            let trimmed = original.trim();
            if trimmed.is_empty() {
                continue;
            }

            // @keep is read before comments are removed because it is metadata.
            for capture in keep.captures_iter(trimmed) {
                for symbol in capture[1].split(|c: char| c == ',' || c.is_whitespace()) {
                    if !symbol.is_empty() {
                        self.blacklist.insert(symbol.to_string());
                    }
                }
            }

            let line = block_comment
                .replace(&line_comment.replace(trimmed, ""), "")
                .to_string();
            if line.is_empty() || line.starts_with('#') {
                continue;
            }

            // Inputs and outputs are external shader interfaces, so renaming them
            // would break the application that binds those values.
            for capture in io.captures_iter(&line) {
                for part in capture[2].split(',') {
                    if let Some(name) = clean_identifier(part, &identifier) {
                        self.blacklist.insert(name);
                    }
                }
            }

            for capture in declaration.captures_iter(&line) {
                let declarations = &capture[1];
                if let Some(function_match) = function.captures(declarations) {
                    self.register(&function_match[1], true);
                    if let (Some(start), Some(end)) =
                        (declarations.find('('), declarations.rfind(')'))
                    {
                        for part in split_top_level(&declarations[start + 1..end]) {
                            if let Some(name) = clean_identifier(&part, &identifier) {
                                self.register(&name, false);
                            }
                        }
                    }
                } else {
                    for part in split_top_level(declarations) {
                        let name = part.split_once('=').map_or(part.as_str(), |(name, _)| name);
                        if let Some(name) = clean_identifier(name, &identifier) {
                            self.register(&name, false);
                        }
                    }
                }
            }
        }
    }

    fn register(&mut self, name: &str, function: bool) {
        if name.trim().is_empty() || self.blacklist.contains(name) || IDENTIFIERS.contains(&name) || self.symbols.contains_key(name)
        {
            return;
        }
        let sequence = if self.separate {
            if function {
                let value = self.functions;
                self.functions += 1;
                value
            } else {
                let value = self.variables;
                self.variables += 1;
                value
            }
        } else {
            let value = self.global;
            self.global += 1;
            value
        };
        let index = self.seed.map_or(sequence as u64, |seed| seeded_index(seed, sequence));
        let prefix = if self.separate {
            if function {
                "f"
            } else {
                "v"
            }
        } else {
            "o"
        };
        self.symbols
            .insert(name.to_string(), format!("{prefix}{index}"));
    }

    pub fn transform(&self, source: &str, minify: bool) -> String {
        transform::transform(source, minify, self)
    }
}

fn seeded_index(seed: u64, sequence: usize) -> u64 {
    // SplitMix64 gives deterministic, dependency-free seeded names.
    let mut value = seed
        .wrapping_add(sequence as u64)
        .wrapping_add(0x9e3779b97f4a7c15);
    value = (value ^ (value >> 30)).wrapping_mul(0xbf58476d1ce4e5b9);
    value = (value ^ (value >> 27)).wrapping_mul(0x94d049bb133111eb);
    (value ^ (value >> 31)) % 5000
}

fn clean_identifier(raw: &str, identifier: &Regex) -> Option<String> {
    let value = Regex::new(r"\[.*?\]").unwrap().replace_all(raw, "");
    identifier
        .captures(value.trim())
        .map(|capture| capture[1].to_string())
}

fn split_top_level(input: &str) -> Vec<String> {
    let mut result = Vec::new();
    let mut current = String::new();
    let mut depth: usize = 0;
    for character in input.chars() {
        match character {
            '(' | '[' | '{' => depth += 1,
            ')' | ']' | '}' => depth = depth.saturating_sub(1),
            _ => {}
        }
        if character == ',' && depth == 0 {
            if !current.trim().is_empty() {
                result.push(current.trim().to_string());
            }
            current.clear();
        } else {
            current.push(character);
        }
    }
    if !current.trim().is_empty() {
        result.push(current.trim().to_string());
    }
    result
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn preserves_interface() {
        let mut context = ObfuscationContext::new(&[], false, None);
        context.collect("uniform vec2 uResolution;\nfloat value = 1.0;");
        let result = context.transform("uniform vec2 uResolution;\nfloat value = 1.0;", true);
        assert!(result.contains("uResolution"));
        assert!(!result.contains("value"));
    }

    #[test]
    fn seeded_output_is_repeatable() {
        let mut first = ObfuscationContext::new(&[], false, Some(42));
        let mut second = ObfuscationContext::new(&[], false, Some(42));
        first.collect("float value = 1.0;");
        second.collect("float value = 1.0;");
        assert_eq!(
            first.transform("float value = 1.0;", true),
            second.transform("float value = 1.0;", true)
        );
    }
}
