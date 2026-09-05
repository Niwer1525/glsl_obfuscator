package com.niwer;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

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
}
