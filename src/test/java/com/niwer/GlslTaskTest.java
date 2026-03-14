package com.niwer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class GlslTaskTest {

    public static void main(String[] args) {
        final String OBFUSCATED_CODE = GlslTask.obfuscate(ExampleShader.TEST_SHADER);
        Utils.print(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromString() {
        final String OBFUSCATED_CODE = GlslTask.obfuscate(ExampleShader.TEST_SHADER);
        assertNotNull(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromNonExistingString() {
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate((String)null));
        assertEquals("", GlslTask.obfuscate(""));
    }

    @Test void testObfuscateFromNonExistingFile() {
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate((File)null));
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate(new File("non_existing_file.glsl")));
    }

    @Test void testObfuscateFromFile() {
        final String OBFUSCATED_CODE = GlslTask.obfuscate(new File("src/test/resources/example_shader.frag"));
        assertNotNull(OBFUSCATED_CODE);
    }

    @Test void testObfuscateFromNonExistingLines() {
        assertThrows(RuntimeException.class, () -> GlslTask.obfuscate((List<String>)null));
        assertEquals("", GlslTask.obfuscate(Collections.emptyList()));
    }
}