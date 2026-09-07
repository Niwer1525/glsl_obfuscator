package com.niwer.glsl_obfuscator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ObfuscationContext {
    private final Map<String, String> SYMBOL_MAP = new LinkedHashMap<>();
    private final Set<String> BLACK_LISTED_SYMBOLS = new HashSet<>(); // uniforms, builtins, in, out

    private final boolean SEPARATE_FUNCS_AND_VARS;
    private final Long SEED;
    private final List<Integer> SHUFFLED_INDICES; // Pool of shuffled indices for obfuscation, used when a seed is provided.

    private int globalCounter = 0;
    private int varCounter = 0;
    private int funcCounter = 0;

    protected ObfuscationContext() { this(null, false, null); }

    protected ObfuscationContext(Collection<String> initialExcludes, boolean separateFuncsAndVars, Long seed) {
        if (initialExcludes != null) this.BLACK_LISTED_SYMBOLS.addAll(initialExcludes);
        this.SEPARATE_FUNCS_AND_VARS = separateFuncsAndVars;
        this.SEED = seed;

        /* Create a pool of shuffled indices for obfuscation, used when a seed is provided. */
        if (seed != null) {
            final Random RANDOM = new Random(seed); // Create a seeded random generator for reproducibility
            final List<Integer> INDICIES = new ArrayList<>(5000);

            for (int i = 0; i < 5000; i++) INDICIES.add(i);
            Collections.shuffle(INDICIES, RANDOM);
            this.SHUFFLED_INDICES = INDICIES;
        } else this.SHUFFLED_INDICES = null;
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
        
        SYMBOL_MAP.computeIfAbsent(name, k -> {
            final int INDEX = getNextIndex(SEPARATE_FUNCS_AND_VARS ? funcCounter++ : globalCounter++);
            return (SEPARATE_FUNCS_AND_VARS ? "f" : "o") + INDEX;
        });
    }

    /**
     * Registers a variable name for obfuscation. If the name is valid and not blacklisted, it will be added to the symbol map with a unique obfuscated name.
     * 
     * @param name The variable name to register for obfuscation.
     */
    public void registerVariable(String name) {
        if (!isValidCandidate(name)) return;

        SYMBOL_MAP.computeIfAbsent(name, k -> {
            final int INDEX = getNextIndex(SEPARATE_FUNCS_AND_VARS ? varCounter++ : globalCounter++);
            return (SEPARATE_FUNCS_AND_VARS ? "v" : "o") + INDEX;
        });
    }

    public Map<String, String> getSymbolMap() {
        return SYMBOL_MAP;
    }

    public Set<String> getBlacklistedSymbols() {
        return BLACK_LISTED_SYMBOLS;
    }

    public long getSeed() {
        if(this.SEED == null) throw new IllegalStateException("No seed was provided for this obfuscation context.");
        return SEED;
    }

    private boolean isValidCandidate(String name) {
        if (name == null || name.isBlank()) return false;
        name = name.trim();
        return !BLACK_LISTED_SYMBOLS.contains(name) && !GlslVariables.isReserved(name);
    }

    private int getNextIndex(int sequenceIndex) {
        if (SHUFFLED_INDICES != null) return SHUFFLED_INDICES.get(sequenceIndex % SHUFFLED_INDICES.size());
        return sequenceIndex;
    }
}