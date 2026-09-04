package com.niwer.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GradleGlslObfuscatorExtensionTest {

    @Test void testDefaultSource() {
        GradleGlslObfuscatorExtension extension = new GradleGlslObfuscatorExtension();
        assertEquals("build", extension.getSource(), "Default source should be 'build'");
    }

    @Test void testSetSource() {
        GradleGlslObfuscatorExtension extension = new GradleGlslObfuscatorExtension();
        extension.setSource("src/main/resources");
        assertEquals("src/main/resources", extension.getSource(), "Source should be updated to 'src/main/resources'");
    }
}
