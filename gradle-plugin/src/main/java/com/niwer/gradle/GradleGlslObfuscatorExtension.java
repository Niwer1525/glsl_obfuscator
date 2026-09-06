package com.niwer.gradle;

import java.util.ArrayList;
import java.util.List;

public class GradleGlslObfuscatorExtension {
    private String source = "build";
    private boolean linked = true; // true = share symbols (#import or #include), false = file by file
    private boolean minify = true; // true = minify GLSL, false = only obfuscate
    private boolean separateFuncsAndVars = false; // true = obfuscate functions and variables separately, false = obfuscate them together
    private Long seed = null; // Seed for randomization, if null a random seed will be generated
    private List<String> excludedSymbols = new ArrayList<>(); // List of symbols to exclude from obfuscation
    private List<String> excludedFiles = new ArrayList<>(); // List of files to exclude from obfuscation

    public String getSource() {
        return source == null || source.isBlank() ? "build" : source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public boolean isSeparateFuncsAndVars() {
        return separateFuncsAndVars;
    }

    public boolean shouldSeparateFuncsAndVars() {
        return separateFuncsAndVars;
    }

    public void setSeparateFuncsAndVars(boolean separateFuncsAndVars) {
        this.separateFuncsAndVars = separateFuncsAndVars;
    }

    public boolean isMinify() {
        return minify;
    }

    public boolean shouldMinify() {
        return minify;
    }

    public void setMinify(boolean minify) {
        this.minify = minify;
    }

    public List<String> getExcludedSymbols() {
        return excludedSymbols;
    }

    public void setExcludedSymbols(List<String> s) { 
        this.excludedSymbols = s;
    }

    public List<String> getExcludedFiles() {
        return excludedFiles;
    }

    public void setExcludedFiles(List<String> f) {
        this.excludedFiles = f;
    }
}