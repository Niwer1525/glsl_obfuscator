package com.niwer.glsl_obfuscator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

public class PatternUtilsTest {

    @Test
    public void testOrGroupInvalidInput() {
        Collection<String> items = Arrays.asList("apple", null, "banana", "", "cherry");
        String expected = "(?:apple|banana|cherry)";
        String actual = PatternUtils.orGroup(items);
        assertEquals(expected, actual);
    }

    @Test
    public void testOrGroup() {
        Collection<String> items = Arrays.asList("apple", "banana", "cherry");
        String expected = "(?:apple|banana|cherry)";
        String actual = PatternUtils.orGroup(items);
        assertEquals(expected, actual);
    }

    @Test
    public void testOptionalPrefixes() {
        Collection<String> modifiers = Arrays.asList("const", "lowp", "mediump");
        String expected = "(?:(?:const|lowp|mediump)\\s+)*";
        String actual = PatternUtils.optionalPrefixes(modifiers);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildIoPattern() {
        Collection<String> storageQualifiers = Arrays.asList("uniform", "attribute", "varying");
        String expectedRegex = "\\b(?:(?:uniform|attribute|varying)|layout\\s*\\([^)]*\\)\\s*(?:in|out)?)\\s+(?:\\w+\\s+)*(\\w+)\\s+([\\w\\s,\\[\\]]+);";
        String actualRegex = PatternUtils.buildIoPattern(storageQualifiers).pattern();
        assertEquals(expectedRegex, actualRegex);
    }
}
