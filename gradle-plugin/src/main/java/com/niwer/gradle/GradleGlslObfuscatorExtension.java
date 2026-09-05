package com.niwer.gradle;

public class GradleGlslObfuscatorExtension {
    private String source = "build";
    private boolean linked = true; // true = share symbols (#import or #include), false = file by file
    private boolean minify = true; // true = minify GLSL, false = only obfuscate

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

    public boolean shouldMinify() {
        return minify;
    }

    public void setMinify(boolean minify) {
        this.minify = minify;
    }
}