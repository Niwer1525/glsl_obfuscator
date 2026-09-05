package com.niwer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlslTask {

    private GlslTask() {}

    /**
     * Obfuscate a GLSL shader file by minifying its content and variable names.
     * 
     * @param file The GLSL shader file to obfuscate.
     * @param shouldMinify If true, the shader file will be minified before obfuscation.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(File file, boolean shouldMinify) { return obfuscate(file, shouldMinify, Set.of()); }

    /**
     * Obfuscate a GLSL shader file by minifying its content and variable names.
     * 
     * @param file The GLSL shader file to obfuscate.
     * @param shouldMinify If true, the shader file will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(File file, boolean shouldMinify, Set<String> initialExcludedSymbols) {
        if (file == null) throw new RuntimeException("File is null");
        if (!file.exists()) throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        return obfuscate(Utils.getLines(file), shouldMinify, initialExcludedSymbols);
    }
    
    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(String content, boolean shouldMinify) { return obfuscate(content, shouldMinify, Set.of()); }


    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(String content, boolean shouldMinify, Set<String> initialExcludedSymbols) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";
        return obfuscate(Utils.getLines(content), shouldMinify, initialExcludedSymbols);
    }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate as a list of lines.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(List<String> content, boolean shouldMinify) { return obfuscate(content, shouldMinify, Set.of()); }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate as a list of lines.
     * @param shouldMinify If true, the shader code will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(List<String> content, boolean shouldMinify, Set<String> initialExcludedSymbols) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";
        if (initialExcludedSymbols == null) throw new RuntimeException("Initial excluded symbols is null");
        return ObfuscatorEngine.obfuscateSingle(content, shouldMinify, initialExcludedSymbols); 
    }

    /**
     * Obfuscate a GLSL shader project by minifying its content and variable names for each shader file.
     * This function is useful for projects with multiple shader files that may share variable names.
     * 
     * @param shaderFiles A list of GLSL shader files to obfuscate.
     * @param shouldMinify If true, the shader files will be minified before obfuscation.
     * @return A map of each shader file to its obfuscated GLSL shader code as a string.
     */
    public static Map<File, String> obfuscateProject(List<File> shaderFiles, boolean shouldMinify) {
        if (shaderFiles == null) throw new RuntimeException("Shader files list is null");
        if (shaderFiles.isEmpty()) return new HashMap<>();
        return obfuscateProject(shaderFiles, shouldMinify, Set.of());
    }

    /**
     * Obfuscate a GLSL shader project by minifying its content and variable names for each shader file.
     * This function is useful for projects with multiple shader files that may share variable names.
     * 
     * @param shaderFiles A list of GLSL shader files to obfuscate.
     * @param shouldMinify If true, the shader files will be minified before obfuscation.
     * @param initialExcludedSymbols A set of symbols to exclude from obfuscation (Generally provided by the user trough a plugin configuration).
     * @return A map of each shader file to its obfuscated GLSL shader code as a string.
     */
    public static Map<File, String> obfuscateProject(List<File> shaderFiles, boolean shouldMinify, Set<String> initialExcludedSymbols) {
        if (shaderFiles == null) throw new RuntimeException("Shader files list is null");
        if (shaderFiles.isEmpty()) return new HashMap<>();
        if (initialExcludedSymbols == null) throw new RuntimeException("Initial excluded symbols is null");
        
        final ObfuscationContext CONTEXT = new ObfuscationContext(initialExcludedSymbols);

        /* First clean and minify all files */
        final Map<File, List<String>> CLEAND_FILES = new HashMap<>();
        for (final File FILE : shaderFiles) {
            final List<String> LINES = Utils.getLines(FILE);
            ObfuscatorEngine.collectSymbols(LINES, CONTEXT); // Collect symbols before cleaning comments (In order to check for @keep annotations)

            final List<String> CLEANED = Utils.getLines(ObfuscatorEngine.clearComments(LINES));
            CLEAND_FILES.put(FILE, CLEANED);
        }

        /* Apply the obfuscation to each file using the global symbol table */
        final Map<File, String> RESULTS = new HashMap<>();
        for (Map.Entry<File, List<String>> entry : CLEAND_FILES.entrySet()) {
            final String OBFUSCATED = ObfuscatorEngine.applyObfuscation(entry.getValue(), CONTEXT);
            RESULTS.put(entry.getKey(), shouldMinify ? ObfuscatorEngine.removeNewLines(OBFUSCATED) : OBFUSCATED);
        }

        return RESULTS;
    }
}
