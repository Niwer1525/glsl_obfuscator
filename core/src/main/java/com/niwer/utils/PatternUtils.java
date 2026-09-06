package com.niwer.utils;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PatternUtils {

    private PatternUtils() {}

    /**
     * Create a non-capturing OR group from a collection of strings.
     * 
     * @param items The collection of strings to include in the OR group.
     * @return A string representing the non-capturing OR group.
     */
    public static String orGroup(Collection<String> items) {
        return items.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("|", "(?:", ")"));
    }

    /**
     * Create an optional and repeatable group of modifiers/prefixes (with spaces):
     * EG: (?:(?:const|lowp|mediump)\s+)*
     * 
     * @param modifiers The collection of modifiers/prefixes to include in the group.
     * @return A string representing the optional and repeatable group of modifiers/prefixes.
     */
    public static String optionalPrefixes(Collection<String> modifiers) {
        return "(?:" + orGroup(modifiers) + "\\s+)*";
    }

    /**
     * Construit la regex pour capturer les déclarations I/O (uniforms, varyings, in, out...).
     */
    /**
     * Creates a regex pattern to capture I/O declarations (uniforms, varyings, in, out...).
     * 
     * @param storageQualifiers A collection of storage qualifiers to include in the regex (e.g., "uniform", "attribute", "varying").
     * @return A compiled Pattern object that matches I/O declarations with the specified storage qualifiers.
     */
    public static Pattern buildIoPattern(Collection<String> storageQualifiers) {
        String qualifiers = orGroup(storageQualifiers);
        String regex = "\\b(?:" + qualifiers + "|layout\\s*\\([^)]*\\)\\s*(?:in|out)?)\\s+(?:\\w+\\s+)*(\\w+)\\s+([\\w\\s,\\[\\]]+);";
        return Pattern.compile(regex);
    }

    /**
     * Construit la regex pour capturer les déclarations de variables et de fonctions.
     */
    public static Pattern buildDeclarationPattern(Collection<String> prefixModifiers, Collection<String> typePatterns) {
        String prefixes = optionalPrefixes(prefixModifiers);
        String types = orGroup(typePatterns);
        String regex = "\\b" + prefixes + types + "\\s+([\\w\\s,\\[\\]=().+/*-]+?)(?=[;{])";
        return Pattern.compile(regex);
    }
}