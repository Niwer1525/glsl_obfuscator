package com.niwer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GlslObfuscatorExtensionTest {

    @Test void testDefaultSource() {
        GlslObfuscatorExtension extension = new GlslObfuscatorExtension();
        assertEquals("build", extension.getSource(), "Default source should be 'build'");
    }

    @Test void testSetSource() {
        GlslObfuscatorExtension extension = new GlslObfuscatorExtension();
        extension.setSource("src/main/resources");
        assertEquals("src/main/resources", extension.getSource(), "Source should be updated to 'src/main/resources'");
    }
}
