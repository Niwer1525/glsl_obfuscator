package com.niwer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ObfuscatorEngineTest {

    @Test
    public void testObfuscateSingleWithNullValues() {
        assertThrows(RuntimeException.class, () -> ObfuscatorEngine.obfuscateSingle(null, true, Set.of(), false));
        assertThrows(RuntimeException.class, () -> ObfuscatorEngine.obfuscateSingle(List.of("test"), true, null, false));
        
        assertDoesNotThrow(() -> ObfuscatorEngine.obfuscateSingle(List.of(), true, Set.of(), false));
        
        assertDoesNotThrow(() -> ObfuscatorEngine.obfuscateSingle(List.of(), true, Set.of(), true));
    }
}
