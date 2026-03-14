package com.niwer;

import java.util.List;

public class Utils {

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
     * Print an object to the console.
     * @param o The object to print.
     */
    public static void print(Object o) { System.out.println(o); }
}
