package com.niwer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test void testGetLines() {
        final String SHADER_CODE = """
        #version 120

        void main() {
            gl_FragColor = vec4(1, 0, 0, 1);
        }
        """;

        final List<String> LINES = Utils.getLines(SHADER_CODE);
        assertNotNull(LINES);
        assertEquals(5, LINES.size());
        assertEquals("#version 120", LINES.get(0));
        assertEquals("", LINES.get(1));
        assertEquals("void main() {", LINES.get(2));
        assertEquals("    gl_FragColor = vec4(1, 0, 0, 1);", LINES.get(3)); // We're checkin the spaces/tabs here.
        assertEquals("}", LINES.get(4));
    }

    @Test void testPrint() {
        // This is just to make sure the print method doesn't throw an exception.
        assertDoesNotThrow(() -> Utils.print("Hello, World!"));
    }

    @Test void testGetLinesFromFile() {
        final var FILE = new java.io.File("src/test/resources/example_shader.frag");
        final List<String> LINES = Utils.getLines(FILE);
        assertNotNull(LINES);
        assertTrue(LINES.size() > 0, "The file should contain lines.");
    }

    @Test void testGetLinesFromNullFile() {
        assertThrows(RuntimeException.class, () -> Utils.getLines((java.io.File)null));
    }
}
