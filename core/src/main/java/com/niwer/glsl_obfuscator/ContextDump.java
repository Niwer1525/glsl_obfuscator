package com.niwer.glsl_obfuscator;

public class ContextDump {

    /***
     * Dumps the contents of the given ObfuscationContext to the console.
     * 
     * @param context The ObfuscationContext to dump.
     */
    public static void dump(ObfuscationContext context) {
        System.out.println("=== Obfuscation Context Dump ===");
        System.out.println();
        
        System.out.println("Blacklisted Symbols: " + context.getBlacklistedSymbols());
        System.out.println();
        System.out.println("Symbol Map: " + context.getSymbolMap());

        System.out.println();
        System.out.println("================================");
    }
}
