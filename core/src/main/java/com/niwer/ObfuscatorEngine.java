package com.niwer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.niwer.utils.Utils;

public class ObfuscatorEngine {

    private ObfuscatorEngine() {}

    /**
     * Obfuscate a single GLSL shader code by minifying its content and variable names.
     * 
     * @param lines The GLSL shader code to obfuscate as a list of lines.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscateSingle(List<String> lines, boolean shouldMinify, Set<String> initialExcludedSymbols, boolean separateFuncsAndVars) {
        if (lines == null) throw new RuntimeException("Lines is null");
        if (lines.isEmpty()) return "";
        if (initialExcludedSymbols == null) throw new RuntimeException("Initial excluded symbols is null");

        final ObfuscationContext CONTEXT = new ObfuscationContext(initialExcludedSymbols, separateFuncsAndVars);
        collectSymbols(lines, CONTEXT);
        
        final List<String> CLEANED = Utils.getLines(clearComments(lines, shouldMinify));
        final String OBFUSCATED = applyObfuscation(CLEANED, CONTEXT, shouldMinify);
        return shouldMinify ? removeNewLines(OBFUSCATED) : OBFUSCATED;
    }

    protected static String clearComments(List<String> lines, boolean shouldMinify) {
        // Join lines into a single string to handle multi-line comments and then process line by line
        String rawCode = String.join("\n", lines);

        // Delete multi-line comments /* ... */
        rawCode = rawCode.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", " ");

        // Delete single-line comments // ... & trim each line
        StringBuilder result = new StringBuilder();
        for (String line : rawCode.split("\n")) {
            /* Delete single-line comments */
            int lineCommentIdx = line.indexOf("//");
            if (lineCommentIdx != -1) line = line.substring(0, lineCommentIdx).trim();

            if(shouldMinify) {
                line = line.trim().replaceAll("\\s+", " ");
                if (!line.isEmpty()) result.append(line).append("\n");
            } else {
                line = line.stripTrailing(); // Remove trailing whitespace but keep leading whitespace for formatting
                result.append(line).append("\n");
            }
        }
        return result.toString();
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

                /* If the declaration contains a function, register it */
                Matcher funcMatcher = Pattern.compile("^([a-zA-Z_]\\w*)\\s*\\(").matcher(declarations);
                if (funcMatcher.find()) {
                    String funcName = funcMatcher.group(1);
                    context.registerFunction(funcName);

                    /* If the line contains parameters (e.g., void foo(int a, float b)) */
                    int startParen = declarations.indexOf('(');
                    int endParen = declarations.lastIndexOf(')');
                    if (startParen != -1 && endParen > startParen) {
                        String params = declarations.substring(startParen + 1, endParen);
                        for (String param : splitTopLevelCommas(params)) {
                            // Ignore les qualificateurs comme 'inout', 'out', 'const'
                            String varName = cleanIdentifier(param);
                            if (!varName.isEmpty()) context.registerVariable(varName);
                        }
                    }
                    continue;
                }

                /* Parse variable declarations */
                for (String part : splitTopLevelCommas(declarations)) {
                    if (part.contains("=")) part = part.substring(0, part.indexOf('='));
                    String name = cleanIdentifier(part);
                    if (!name.isEmpty()) context.registerVariable(name);
                }
            }
        }
    }

    /**
     * Split a string on top-level commas (not enclosed in parentheses, brackets, or braces).
     * This is useful for parsing GLSL declarations where commas may appear inside constructors or function calls
     * 
     * @param input
     * @return
     */
    protected static List<String> splitTopLevelCommas(String input) {
        if (input == null || input.isBlank()) return List.of();

        List<String> tokens = new java.util.ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') depth = Math.max(0, depth - 1);

            /* We only split on commas that are not enclosed in any containers */
            if (c == ',' && depth == 0) {
                String token = current.toString().trim();
                if (!token.isEmpty()) tokens.add(token);
                current.setLength(0);
            } else current.append(c);
        }

        String lastToken = current.toString().trim();
        if (!lastToken.isEmpty()) tokens.add(lastToken);

        return tokens;
    }

    protected static String applyObfuscation(List<String> lines, ObfuscationContext context, boolean shouldMinify) {
        StringBuilder result = new StringBuilder();
        Map<String, String> symbols = context.getSymbolMap();
        Set<String> blacklist = context.getBlacklistedSymbols();
        Pattern wordPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b"); // Capture valid identifiers (starting with a letter or underscore, followed by letters, digits, or underscores)

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (!shouldMinify) result.append("\n"); // Preserve empty lines for formatting if not minifying
                continue;
            }

            /* Keep preprocessor directives intact (e.g., #import, #version, etc.) */
            if (trimmed.startsWith("#")) {
                result.append(shouldMinify ? trimmed : line).append("\n");
                continue;
            }

            String lineToProcess = shouldMinify ? trimmed : line; // Use trimmed line for minification, original line for formatting
            Matcher matcher = wordPattern.matcher(lineToProcess);
            StringBuilder rewrittenLine = new StringBuilder();

            while (matcher.find()) {
                String word = matcher.group(1);
                int start = matcher.start();

                /* Check if the word is a property access (e.g., "obj.prop" or "obj['prop']") */
                boolean isPropertyAccess = false;
                if (start > 0) {
                    char prevChar = lineToProcess.charAt(start - 1);
                    if (prevChar == '.') isPropertyAccess = true;
                    else if (prevChar == ' ' || prevChar == '\t') {
                        int p = start - 1;
                        while (p >= 0 && Character.isWhitespace(lineToProcess.charAt(p))) p--;
                        if (p >= 0 && lineToProcess.charAt(p) == '.') isPropertyAccess = true;
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

            if (line.startsWith("#")) { // Keep preprocessor directives on their own line
                if (code.length() > 0 && code.charAt(code.length() - 1) != '\n') code.append('\n');
                code.append(line).append("\n");
                continue;
            }

            if (!line.isEmpty()) code.append(line).append(" ");
        }
        return code.toString().trim();
    }
}