package com.niwer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObfuscationContext {
    private final Map<String, String> SYMBOL_MAP = new HashMap<>();
    private final Set<String> BLACK_LISTED_SYMBOLS = new HashSet<>(); // uniforms, builtins, in, out
    private int counter = 0;

    public void blacklist(String name) {
        BLACK_LISTED_SYMBOLS.add(name);
    }

    public void registerSymbol(String name) {
        if (!BLACK_LISTED_SYMBOLS.contains(name) && !GlslVariables.isReserved(name)) SYMBOL_MAP.computeIfAbsent(name, k -> "v" + (counter++));
    }

    public Map<String, String> getSymbolMap() {
        return SYMBOL_MAP;
    }

    public Set<String> getBlacklistedSymbols() {
        return BLACK_LISTED_SYMBOLS;
    }
}