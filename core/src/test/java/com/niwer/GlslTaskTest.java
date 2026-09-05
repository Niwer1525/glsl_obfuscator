package com.niwer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class GlslTaskTest {

    public static void main(String[] args) {
        final String OBFUSCATED_CODE = GlslTask.obfuscate(ExampleShader.TEST_SHADER, true);
        Utils.print(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromString() {
        final String OBFUSCATED_CODE = GlslTask.obfuscate(ExampleShader.TEST_SHADER, true);
        assertNotNull(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromNonExistingString() {
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate((String)null, true));
        assertEquals("", GlslTask.obfuscate("", true));
    }

    @Test void testObfuscateFromNonExistingFile() {
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate((File)null, true));
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate(new File("non_existing_file.glsl"), true));
    }

    @Test void testObfuscateFromFile() {
        final String OBFUSCATED_CODE = GlslTask.obfuscate(new File("src/test/resources/example_shader.frag"), true);
        assertNotNull(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromFiles() {
        final List<File> COLLECTION = new ArrayList<File>();
        Collections.addAll(COLLECTION,
            new File("src/test/resources/import_example/shader_with_import.glsl"),
            new File("src/test/resources/import_example/model_calculator.glsl"),
            new File("src/test/resources/import_example/math.glsl")
        );
        final var OBFUSCATED_CODE = GlslTask.obfuscateProject(COLLECTION, true);
        assertNotNull(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromNonExistingLines() {
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate((List<String>)null, true));
        assertEquals("", GlslTask.obfuscate(Collections.emptyList(), true));
    }
}