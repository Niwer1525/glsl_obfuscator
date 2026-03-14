package com.niwer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GlslVariablesTest {

    @Test void testIsKeyword() {
        assertTrue(GlslVariables.isKeyword("uniform"));
        assertFalse(GlslVariables.isKeyword("myVariable"));
    }

    @Test void testIsSwizzle() {
        assertTrue(GlslVariables.isSwizzle("xy"));
        assertFalse(GlslVariables.isSwizzle("myVariable"));
    }

    @Test void testIsBuiltinProperty() {
        assertTrue(GlslVariables.isBuiltinProperty("diffuse"));
        assertFalse(GlslVariables.isBuiltinProperty("myVariable"));
    }

    @Test void testIsReserved() {
        assertTrue(GlslVariables.isReserved("uniform"));
        assertTrue(GlslVariables.isReserved("xy"));
        assertTrue(GlslVariables.isReserved("diffuse"));
        assertFalse(GlslVariables.isReserved("myVariable"));
    }
}
