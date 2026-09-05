package com.niwer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObfuscatorEngine {

    private ObfuscatorEngine() {}

    /**
     * Obfuscate a single GLSL shader code by minifying its content and variable names.
     * 
     * @param lines The GLSL shader code to obfuscate as a list of lines.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscateSingle(List<String> lines, boolean shouldMinify, Set<String> initialExcludedSymbols) {
        if (lines == null) throw new RuntimeException("Lines is null");
        if (lines.isEmpty()) return "";
        if (initialExcludedSymbols == null) throw new RuntimeException("Initial excluded symbols is null");

        final ObfuscationContext CONTEXT = new ObfuscationContext(initialExcludedSymbols);
        collectSymbols(lines, CONTEXT);
        
        final List<String> CLEANED = Utils.getLines(clearComments(lines));
        final String OBFUSCATED = applyObfuscation(CLEANED, CONTEXT);
        return shouldMinify ? removeNewLines(OBFUSCATED) : OBFUSCATED;
    }

    protected static String clearComments(List<String> lines) {
        // Join lines into a single string to handle multi-line comments and then process line by line
        String rawCode = String.join("\n", lines);

        // Delete multi-line comments /* ... */
        rawCode = rawCode.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", " ");

        // Delete single-line comments // ... & trim each line
        StringBuilder minified = new StringBuilder();
        for (String line : rawCode.split("\n")) {
            line = line.trim();

            /* Delete single-line comments */
            int lineCommentIdx = line.indexOf("//");
            if (lineCommentIdx != -1) line = line.substring(0, lineCommentIdx).trim();

            /* Remove extra whitespace and normalize spaces */
            line = line.replaceAll("\\s+", " ");

            if (!line.isEmpty()) minified.append(line).append("\n");
        }
        return minified.toString();
    }

    protected static void collectSymbols(List<String> lines, ObfuscationContext context) {
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher keepMatcher = GlslVariables.KEEP_DIRECTIVE_PATTERN.matcher(line);
            while (keepMatcher.find()) {
                String[] symbols = keepMatcher.group(1).split("[,\\s]+");
                for (String sym : symbols) {
                    sym = sym.trim();
                    if (!sym.isEmpty()) context.blacklist(sym);
                }
            }

            line = line.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "").trim(); // Remove comments for symbol collection
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue; // Ignore preprocessor directives

            /* Identify I/O variables (uniforms, varyings, in, out) */
            Matcher ioMatcher = GlslVariables.IO_PATTERN.matcher(line);
            while (ioMatcher.find()) {
                String varList = ioMatcher.group(2);
                for (String part : varList.split(",")) {
                    String name = cleanIdentifier(part);
                    if (!name.isEmpty()) context.blacklist(name);
                }
            }

            /* Identify local, global variables, and function declarations */
            Matcher declMatcher = GlslVariables.DECLARATION_PATTERN.matcher(line);
            while (declMatcher.find()) {
                String declarations = declMatcher.group(1);

                /* Handle multiple declarations or assignments: e.g., "a = 5.0, b, c" */
                for (String part : declarations.split(",")) {
                    if (part.contains("=")) part = part.substring(0, part.indexOf('=')); // Keep only the variable name before the assignment                    
                    if (part.contains("(")) part = part.substring(0, part.indexOf('(')); // Keep only the function name before the parenthesis

                    String name = cleanIdentifier(part);
                    if (!name.isEmpty()) context.registerSymbol(name);
                }
            }
        }
    }

    protected static String applyObfuscation(List<String> lines, ObfuscationContext context) {
        StringBuilder result = new StringBuilder();
        Map<String, String> symbols = context.getSymbolMap();
        Set<String> blacklist = context.getBlacklistedSymbols();
        Pattern wordPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b"); // Capture valid identifiers (starting with a letter or underscore, followed by letters, digits, or underscores)

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            /* Keep preprocessor directives intact (e.g., #import, #version, etc.) */
            if (line.startsWith("#")) {
                result.append(line).append("\n");
                continue;
            }

            Matcher matcher = wordPattern.matcher(line);
            StringBuilder rewrittenLine = new StringBuilder();

            while (matcher.find()) {
                String word = matcher.group(1);
                int start = matcher.start();

                /* Check if the word is a property access (e.g., "obj.prop" or "obj['prop']") */
                boolean isPropertyAccess = false;
                if (start > 0) {
                    char prevChar = line.charAt(start - 1);
                    if (prevChar == '.') isPropertyAccess = true;
                    else if (prevChar == ' ' || prevChar == '\t') {
                        int p = start - 1;
                        while (p >= 0 && Character.isWhitespace(line.charAt(p))) p--;
                        if (p >= 0 && line.charAt(p) == '.') isPropertyAccess = true;
                    }
                }

                /* Replacement if: it's not a property, it's in our table and not blacklisted */
                if (!isPropertyAccess && symbols.containsKey(word) && !blacklist.contains(word)) matcher.appendReplacement(rewrittenLine, Matcher.quoteReplacement(symbols.get(word)));
                else matcher.appendReplacement(rewrittenLine, Matcher.quoteReplacement(word));
            }
            matcher.appendTail(rewrittenLine);
            result.append(rewrittenLine).append("\n");
        }
        return result.toString();
    }

    private static String cleanIdentifier(String raw) {
        raw = raw.replaceAll("\\[.*?\\]", "").trim(); // Remove array brackets and trim whitespace (e.g., "array[10]" -> "array")
        Matcher m = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b").matcher(raw); // Only keep valid identifier characters
        return m.find() ? m.group(1) : "";
    }

    protected static String removeNewLines(String code) {
        return removeNewLines(Utils.getLines(code));
    }

    private static String removeNewLines(List<String> lines) {
        final StringBuilder code = new StringBuilder();
        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("#")) {
                if (code.length() > 0 && code.charAt(code.length() - 1) != '\n') code.append('\n');
                code.append(line).append("\n");
                continue;
            }

            if (!line.isEmpty()) code.append(line).append(" ");
        }
        return code.toString().trim();
    }
}