package com.niwer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ObfuscatorEngineTest {

    @Test
    public void testObfuscateSingleWithNullValues() {
        assertThrows(RuntimeException.class, () -> ObfuscatorEngine.obfuscateSingle(null, true, Set.of(), false, null));
        assertThrows(RuntimeException.class, () -> ObfuscatorEngine.obfuscateSingle(List.of("test"), true, null, false, null));
        
        assertDoesNotThrow(() -> ObfuscatorEngine.obfuscateSingle(List.of(), true, Set.of(), false, null));
        
        assertDoesNotThrow(() -> ObfuscatorEngine.obfuscateSingle(List.of(), true, Set.of(), true, null));
    }

    @Test
    public void testObfuscateSingleWithNullValuesAndSeed() {
        assertThrows(RuntimeException.class, () -> ObfuscatorEngine.obfuscateSingle(null, true, Set.of(), false, 255L));
        assertThrows(RuntimeException.class, () -> ObfuscatorEngine.obfuscateSingle(List.of("test"), true, null, false, 255L));
        
        assertDoesNotThrow(() -> ObfuscatorEngine.obfuscateSingle(List.of(), true, Set.of(), false, 255L));
        
        assertDoesNotThrow(() -> ObfuscatorEngine.obfuscateSingle(List.of(), true, Set.of(), true, 255L));
    }
}
