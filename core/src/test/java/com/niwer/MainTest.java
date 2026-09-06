package com.niwer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        {
            /* Obfuscate a project with multiple shader files */
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

        {
            /* Obfuscate a single shader file */
            final String OBFUSCATED_CODE = GlslObfuscator.obfuscate(new File("core/src/test/resources/example_shader.frag"), true, false, 255L);
            System.out.println("Obfuscated code for single-file:");
            System.out.println(OBFUSCATED_CODE);
            System.out.println();
        }

        {
            /* Obfuscate a single shader file (Multi-line Macro test) */
            final String OBFUSCATED_CODE = GlslObfuscator.obfuscate(new File("core/src/test/resources/multi_lines_macro.frag"), true, false, 255L);
            System.out.println("Obfuscated code for single-file:");
            System.out.println(OBFUSCATED_CODE);
            System.out.println();
        }
    }
}
