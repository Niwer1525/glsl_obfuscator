package com.niwer.glsl_obfuscator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ObfuscationContextTest {

    @Test
    public void testObfuscationContextInitialization() {
        ObfuscationContext context = new ObfuscationContext();
        assertNotNull(context.getSymbolMap());
        assertNotNull(context.getBlacklistedSymbols());
        assertTrue(context.getSymbolMap().isEmpty());
        assertTrue(context.getBlacklistedSymbols().isEmpty());
    }
    
    @Test
    public void testObfuscationContextWithSeed() {
        ObfuscationContext context = new ObfuscationContext(null, false, 2333L);
        assertNotNull(context.getSymbolMap());
        assertNotNull(context.getBlacklistedSymbols());
        assertTrue(context.getSymbolMap().isEmpty());
        assertTrue(context.getBlacklistedSymbols().isEmpty());
    }

    @Test
    public void testObfuscationContextWithInvalidInitialExcludes() {
        ObfuscationContext context = new ObfuscationContext(null, false, null);
        assertNotNull(context.getSymbolMap());
        assertNotNull(context.getBlacklistedSymbols());
        assertTrue(context.getSymbolMap().isEmpty());
        assertTrue(context.getBlacklistedSymbols().isEmpty());
    }
}
