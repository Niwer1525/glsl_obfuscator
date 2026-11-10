use regex::Regex;

use crate::context::ObfuscationContext;

/*
    Removes comments, rewrites identifiers, and optionally joins output lines.
*/
pub(crate) fn transform(source: &str, minify: bool, context: &ObfuscationContext) -> String {
    let cleaned = clear_comments(source, minify);
    let word = Regex::new(r"\b([a-zA-Z_][a-zA-Z0-9_]*)\b").unwrap();
    let mut output = String::new();
    for line in cleaned.lines() {
        let trimmed = line.trim();
        if trimmed.is_empty() {
            if !minify {
                output.push('\n');
            }
            continue;
        }
        output.push_str(&rewrite(
            if minify { trimmed } else { line },
            &word,
            context,
        ));
        output.push('\n');
    }
    if minify {
        remove_newlines(&output)
    } else {
        output
    }
}

fn clear_comments(source: &str, minify: bool) -> String {
    // Joining is unnecessary here because the regex handles the full source,
    // including block comments that span multiple lines.
    let block = Regex::new(r"/\*[^*]*\*+(?:[^/*][^*]*\*+)*/")
        .unwrap()
        .replace_all(source, " ");
    let mut result = String::new();
    for original in block.split('\n') {
        let line = original.split_once("//").map_or(original, |(line, _)| line);
        if minify {
            let line = line.split_whitespace().collect::<Vec<_>>().join(" ");
            if !line.is_empty() {
                result.push_str(line.trim());
                result.push('\n');
            }
        } else {
            result.push_str(line.trim_end());
            result.push('\n');
        }
    }
    result
}

fn rewrite(line: &str, word: &Regex, context: &ObfuscationContext) -> String {
    word.replace_all(line, |capture: &regex::Captures| {
        let start = capture.get(0).unwrap().start();
        // A name following '.' is a member/property, not a local symbol.
        let property = line[..start].chars().rev().find(|c| !c.is_whitespace()) == Some('.');
        let name = &capture[1];
        if !property && !context.blacklist.contains(name) {
            context
                .symbols
                .get(name)
                .cloned()
                .unwrap_or_else(|| name.to_string())
        } else {
            name.to_string()
        }
    })
    .to_string()
}

fn remove_newlines(source: &str) -> String {
    let mut result = String::new();
    for line in source
        .lines()
        .map(str::trim)
        .filter(|line| !line.is_empty())
    {
        if line.starts_with('#') {
            // Preprocessor directives remain line-oriented; ordinary GLSL can join.
            if !result.is_empty() {
                result.push('\n');
            }
            result.push_str(line);
            result.push('\n');
        } else {
            result.push_str(line);
            result.push(' ');
        }
    }
    result.trim().to_string()
}
