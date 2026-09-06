package com.niwer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class MainTest {

    public static void main(String[] args) {
        switch (askUserForInt("Select test case", 1, 3)) {
            case 1 -> testObfuscateMultipleFiles();
            case 2 -> testObfuscateSingleFile();
            case 3 -> testObfuscateSingleFileMultiLineMacro();
        }
    }

    private static void testObfuscateMultipleFiles() {
        final List<File> COLLECTION = new ArrayList<File>();
        Collections.addAll(COLLECTION,
            new File("core/src/test/resources/import_example/shader_with_import.glsl"),
            new File("core/src/test/resources/import_example/model_calculator.glsl"),
            new File("core/src/test/resources/import_example/math.glsl")
        );
        final var OBFUSCATED_CODE = GlslObfuscator.obfuscateProject(COLLECTION, true, false);

        System.out.println("Obfuscated code for multiple-files:");
        for (var entry : OBFUSCATED_CODE.entrySet()) {
            System.out.println("File: " + entry.getKey().getName());
            System.out.println(entry.getValue());
            System.out.println();
        }
        System.out.println();
    }

    private static void testObfuscateSingleFile() {
        final String OBFUSCATED_CODE = GlslObfuscator.obfuscate(new File("core/src/test/resources/example_shader.frag"), true, false, 255L);
        System.out.println("Obfuscated code for single-file:");
        System.out.println(OBFUSCATED_CODE);
        System.out.println();
    }

    private static void testObfuscateSingleFileMultiLineMacro() {
        final String OBFUSCATED_CODE = GlslObfuscator.obfuscate(new File("core/src/test/resources/multi_lines_macro.frag"), true, false, 255L);
        System.out.println("Obfuscated code for single-file (Multi-line Macro test):");
        System.out.println(OBFUSCATED_CODE);
        System.out.println();
    }

    private static int askUserForInt(String prompt, int min, int max) {
        try(final Scanner SCANNER = new Scanner(System.in)) {
            int value;
            while (true) {
                System.out.print(prompt + " [" + min + "-" + max + "]: ");
                if (SCANNER.hasNextInt()) {
                    value = SCANNER.nextInt();
                    if (value >= min && value <= max) break;
                } else SCANNER.next(); // Consume the invalid input
                System.out.println("Invalid input. Please enter an integer between " + min + " and " + max + ".");
            }
            return value;
        }
    }
}
