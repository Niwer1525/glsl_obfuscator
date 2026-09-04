package com.niwer;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlslTask {

    private GlslTask() {}

    /**
     * Obfuscate a GLSL shader file by minifying its content and variable names.
     * 
     * @param file The GLSL shader file to obfuscate.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(File file) {
        if (file == null) throw new RuntimeException("File is null");
        if (!file.exists()) throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        return obfuscate(getLinesFromFile(file)); // Read the file and minify its content
    }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(String content) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";
        return obfuscate(Utils.getLines(content)); // Split the content into lines and minify it
    }

    /**
     * Obfuscate a GLSL shader code by minifying its content and variable names.
     * 
     * @param content The GLSL shader code to obfuscate as a list of lines.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(List<String> content) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";
        return ObfuscatorEngine.obfuscateSingle(content); // Remove new lines
    }

    /**
     * Obfuscate a GLSL shader project by minifying its content and variable names for each shader file.
     * This function is usful for projects with multiple shader files that may share variable names.
     * 
     * @param shaderFiles A list of GLSL shader files to obfuscate.
     * @return A map of each shader file to its obfuscated GLSL shader code as a string.
     */
    public static Map<File, String> obfuscateProject(List<File> shaderFiles) {
        final ObfuscationContext CONTEXT = new ObfuscationContext();

        /* First clean and minify all files */
        final Map<File, List<String>> CLEAND_FILES = new HashMap<>();
        for (final File FILE : shaderFiles) {
            final List<String> MINIFIED_CODE = Utils.getLines(ObfuscatorEngine.minify(getLinesFromFile(FILE)));
            CLEAND_FILES.put(FILE, MINIFIED_CODE);
            ObfuscatorEngine.collectSymbols(MINIFIED_CODE, CONTEXT);
        }

        /* Apply the obfuscation to each file using the global symbol table */
        final Map<File, String> RESULTS = new HashMap<>();
        for (Map.Entry<File, List<String>> entry : CLEAND_FILES.entrySet()) {
            String obfuscatedCode = ObfuscatorEngine.applyObfuscation(entry.getValue(), CONTEXT);
            String finalContent = ObfuscatorEngine.removeNewLines(Utils.getLines(obfuscatedCode));
            RESULTS.put(entry.getKey(), finalContent);
        }

        return RESULTS;
    }

    private static List<String> getLinesFromFile(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }
}
