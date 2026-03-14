package com.niwer;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class GlslTask {

    /**
     * Obfuscate a GLSL shader file by minifying its content and variable names.
     * 
     * @param file The GLSL shader file to obfuscate.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(File file) {
        if (file == null) throw new RuntimeException("File is null");
        if (!file.exists()) throw new RuntimeException("File does not exist: " + file.getAbsolutePath());

        try {
            return obfuscate(Files.readAllLines(file.toPath())); // Read the file and minify its content
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
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
     * @param content The GLSL shader code to obfuscate as a list of lines.
     * @return The obfuscated GLSL shader code as a string.
     */
    public static String obfuscate(List<String> content) {
        if (content == null) throw new RuntimeException("Content is null");
        if (content.isEmpty()) return "";

        final List<String> MINIFED_LINES = Utils.getLines(ObfuscatorEngine.minify(content)); // Minify the GLSL code
        final List<String> MNIFIED_VARIABLES = Utils.getLines(ObfuscatorEngine.minifyVariableNames(MINIFED_LINES)); // Minify variable names

        return ObfuscatorEngine.removeNewLines(MNIFIED_VARIABLES); // Remove new lines
    }
}
