package com.niwer.glsl_obfuscator;

import com.niwer.glsl_obfuscator.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Public Java facade for the shared Rust obfuscation engine.
 */
public final class GlslObfuscator {
    private GlslObfuscator() {}

    /**
     * Obfuscate a GLSL shader file by minifying its content and variable names.
     * 
     * @param file The GLSL shader file to obfuscate.
     * @param shouldMinify If true, the shader file will be minified before obfuscation.
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed value to ensure reproducible symbol generation across builds.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(File file, boolean shouldMinify, boolean separateFuncsAndVars, Long seed) {
        return obfuscate(file, shouldMinify, Set.of(), separateFuncsAndVars, seed);
    }

    /**
     * Obfuscate a GLSL shader file by minifying its content and variable names.
     * 
     * @param file The GLSL shader file to obfuscate.
     * @param shouldMinify If true, the shader file will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed value to ensure reproducible symbol generation across builds.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(File file, boolean shouldMinify, Set<String> initialExcludedSymbols, boolean separateFuncsAndVars, Long seed) {
        if (file == null) throw new RuntimeException("File is null");
        if (!file.exists()) throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        return obfuscate(Utils.getLines(file), shouldMinify, initialExcludedSymbols, separateFuncsAndVars, seed);
    }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed value to ensure reproducible symbol generation across builds.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(String content, boolean shouldMinify, boolean separateFuncsAndVars, Long seed) { return obfuscate(content, shouldMinify, Set.of(), separateFuncsAndVars, seed); }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed value to ensure reproducible symbol generation across builds.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(String content, boolean shouldMinify, Set<String> initialExcludedSymbols, boolean separateFuncsAndVars, Long seed) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";
        return obfuscate(Utils.getLines(content), shouldMinify, initialExcludedSymbols, separateFuncsAndVars, seed);
    }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate as a list of lines.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed value to ensure reproducible symbol generation across builds.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(List<String> content, boolean shouldMinify, boolean separateFuncsAndVars, Long seed) { return obfuscate(content, shouldMinify, Set.of(), separateFuncsAndVars, seed); }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate as a list of lines.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed value to ensure reproducible symbol generation across builds.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(List<String> content, boolean shouldMinify, Set<String> initialExcludedSymbols, boolean separateFuncsAndVars, Long seed) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";
        if (initialExcludedSymbols == null) throw new RuntimeException("Initial excluded symbols is null");
        try (RustGlslEngine engine = RustGlslEngine.create(initialExcludedSymbols, separateFuncsAndVars, seed)) {
            String source = String.join("\n", content);
            engine.collect(source);
            return engine.transform(source, shouldMinify);
        }
    }

    /**
     * Obfuscate a GLSL shader project by minifying its content and variable names for each shader file.
     * This function is useful for projects with multiple shader files that may share variable names.
     * 
     * @param shaderFiles A list of GLSL shader files to obfuscate.
     * @param shouldMinify If true, the shader files will be minified before obfuscation.
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @return A map of each shader file to its obfuscated GLSL shader code as a string.
     */
    public static Map<File, String> obfuscateProject(List<File> shaderFiles, boolean shouldMinify, boolean separateFuncsAndVars) {
        if (shaderFiles == null) throw new RuntimeException("Shader files list is null");
        if (shaderFiles.isEmpty()) return new HashMap<>();
        return obfuscateProject(shaderFiles, shouldMinify, Set.of(), separateFuncsAndVars, null);
    }

    /**
     * Obfuscate a GLSL shader project by minifying its content and variable names for each shader file.
     * This function is useful for projects with multiple shader files that may share variable names.
     * 
     * @param shaderFiles A list of GLSL shader files to obfuscate.
     * @param shouldMinify If true, the shader files will be minified before obfuscation.
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed to use for the obfuscation.
     * @return A map of each shader file to its obfuscated GLSL shader code as a string.
     */
    public static Map<File, String> obfuscateProject(List<File> shaderFiles, boolean shouldMinify, boolean separateFuncsAndVars, Long seed) {
        if (shaderFiles == null) throw new RuntimeException("Shader files list is null");
        if (shaderFiles.isEmpty()) return new HashMap<>();
        return obfuscateProject(shaderFiles, shouldMinify, Set.of(), separateFuncsAndVars, seed);
    }

    /**
     * Obfuscate a GLSL shader project by minifying its content and variable names for each shader file.
     * This function is useful for projects with multiple shader files that may share variable names.
     * 
     * @param shaderFiles A list of GLSL shader files to obfuscate.
     * @param shouldMinify If true, the shader files will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @param separateFuncsAndVars If true, function and variable names will be obfuscated separately.
     * @param seed The seed to use for the obfuscation.
     * @return A map of each shader file to its obfuscated GLSL shader code as a string.
     */
    public static Map<File, String> obfuscateProject(List<File> shaderFiles, boolean shouldMinify, Set<String> initialExcludedSymbols, boolean separateFuncsAndVars, Long seed) {
        if (shaderFiles == null) throw new RuntimeException("Shader files list is null");
        if (shaderFiles.isEmpty()) return new HashMap<>();
        if (initialExcludedSymbols == null) throw new RuntimeException("Initial excluded symbols is null");

        /* Create the context and sort files */    
        List<File> sortedFiles = shaderFiles.stream().sorted((first, second) -> first.getName().compareTo(second.getName())).toList();
        Map<File, String> sources = new LinkedHashMap<>();
        try (RustGlslEngine engine = RustGlslEngine.create(initialExcludedSymbols, separateFuncsAndVars, seed)) {
            for (File file : sortedFiles) {
                if (!file.exists()) throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
                String source = String.join("\n", Utils.getLines(file));
                engine.collect(source);
                sources.put(file, source);
            }

            Map<File, String> results = new LinkedHashMap<>();
            for (Map.Entry<File, String> entry : sources.entrySet()) results.put(entry.getKey(), engine.transform(entry.getValue(), shouldMinify));
            return results;
        }
    }
}
