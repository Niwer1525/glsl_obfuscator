package com.niwer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObfuscationContext {
    private final Map<String, String> SYMBOL_MAP = new HashMap<>();
    private final Set<String> BLACK_LISTED_SYMBOLS = new HashSet<>(); // uniforms, builtins, in, out

    private final boolean SEPARATE_FUNCS_AND_VARS;

    private int globalCounter = 0;
    private int varCounter = 0;
    private int funcCounter = 0;

    protected ObfuscationContext() { this(null, false); }

    protected ObfuscationContext(Collection<String> initialExcludes, boolean separateFunctionAndVariableCounters) {
        if (initialExcludes != null) this.BLACK_LISTED_SYMBOLS.addAll(initialExcludes);
        this.SEPARATE_FUNCS_AND_VARS = separateFunctionAndVariableCounters;
    }

    public void blacklist(String name) {
        BLACK_LISTED_SYMBOLS.add(name);
    }

    /**
     * Registers a function name for obfuscation. If the name is valid and not blacklisted, it will be added to the symbol map with a unique obfuscated name.
     * 
     * @param name The function name to register for obfuscation.
     */
    public void registerFunction(String name) {
        if (!isValidCandidate(name)) return;

        if(SEPARATE_FUNCS_AND_VARS) SYMBOL_MAP.computeIfAbsent(name, k -> "f" + (funcCounter++));
        else SYMBOL_MAP.computeIfAbsent(name, k -> "o" + (globalCounter++));
    }

    /**
     * Registers a variable name for obfuscation. If the name is valid and not blacklisted, it will be added to the symbol map with a unique obfuscated name.
     * 
     * @param name The variable name to register for obfuscation.
     */
    public void registerVariable(String name) {
        if (!isValidCandidate(name)) return;

        if(SEPARATE_FUNCS_AND_VARS) SYMBOL_MAP.computeIfAbsent(name, k -> "v" + (varCounter++));
        else SYMBOL_MAP.computeIfAbsent(name, k -> "o" + (globalCounter++));
    }

    public Map<String, String> getSymbolMap() {
        return SYMBOL_MAP;
    }

    public Set<String> getBlacklistedSymbols() {
        return BLACK_LISTED_SYMBOLS;
    }

    private boolean isValidCandidate(String name) {
        if (name == null || name.isBlank()) return false;
        name = name.trim();
        return !BLACK_LISTED_SYMBOLS.contains(name) && !GlslVariables.isReserved(name);
    }
}