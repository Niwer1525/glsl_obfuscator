package com.niwer.glsl_obfuscator.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    private Utils() {}

    /**
     * This will split a shader code into lines.
     * 
     * @param shaderCode The shader code to split into lines.
     * @return A list of strings, each representing a line of the shader code.
     */
    public static List<String> getLines(String shaderCode) {
        return List.of(shaderCode.split("\n")); // Split the shader code into lines
    }

    /**
     * This will read a file and return its content as a list of lines.
     * 
     * @param file The file to read.
     * @return A list of strings, each representing a line of the file.
     */
    public static List<String> getLines(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Print an object to the console.
     * @param o The object to print.
     */
    public static void print(Object o) { System.out.println(o); }

    /**
     * Merge two sets into a new set containing all unique elements from both.
     * 
     * @param a The first set to merge.
     * @param b The second set to merge.
     * @return A new set containing all unique elements from both input sets.
     */
    public static Set<String> mergeSets(Set<String> a, Set<String> b) {
        Set<String> merged = new HashSet<>(a);
        merged.addAll(b);
        return merged;
    }
}
