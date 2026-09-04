package com.niwer;

import java.util.Set;
import java.util.regex.Pattern;

public class GlslVariables {

    private GlslVariables() {}

    // private static final Pattern PREPROCESSOR_PATTERN = Pattern.compile("^\\s*#.*$");
    public static final String PREPROCESSOR_DIRECTIVE_REGEX = "^\\s*#\\s*\\w+.*$";
    public static final Pattern IO_PATTERN = Pattern.compile(
        "\\b(?:uniform|attribute|varying|in|out|layout\\s*\\([^)]*\\)\\s*(?:in|out)?)\\s+(?:\\w+\\s+)*(\\w+)\\s+([\\w\\s,\\[\\]]+);"
    ); // This regex captures uniforms, attributes, varyings, in, out (including optional qualifiers like 'flat', 'smooth', 'centroid')
    public static final Pattern DECLARATION_PATTERN = Pattern.compile(
        "\\b(?:void|int|uint|bool|float|double|vec[234]|u?ivec[234]|bvec[234]|dvec[234]|mat[234](?:x[234])?|sampler[123]D|samplerCube)\\s+([\\w\\s,\\[\\]=().+/*-]+?)(?=[;{])"
    ); // This regex captures variable declarations, including multiple declarations in a single line (e.g., "float a, b = 1.0, c[2];")

    public static final Set<String> GLSL_KEYWORDS = Set.of(
        "attribute", "const", "uniform", "varying", "break", "continue",
        "do", "else", "for", "if", "discard", "return", "switch",
        "case", "default", "in", "out", "inout", "float", "int",
        "void", "bool", "true", "false", "mat2", "mat3",
        "mat4", "vec2", "vec3", "vec4", "while",
        "sampler2D", "samplerCube", "main",
        "gl_FragColor", "gl_FragCoord", "gl_Position", "gl_VertexID",
        "texture2D", "textureCube",
        "dot", "mix", "clamp", "fract", "sin", "cos"
    );

    public static final Set<String> GLSL_SWIZZLES = Set.of("x", "y", "z", "w", "r", "g", "b", "a", "xy", "xz", "yz", "rgb", "rgba", "st", "stp");
    
    public static final Set<String> GLSL_BUILTIN_PROPERTIES = Set.of(
        "diffuse", "ambient", "specular", "position", "spotDirection", "spotExponent",
        "spotCutoff", "constantAttenuation", "linearAttenuation", "quadraticAttenuation"
    );

    /**
     * Check if a word is a GLSL keyword.
     * @param word The word to check.
     * @return True if the word is a GLSL keyword, false otherwise.
     */
    protected static boolean isKeyword(String word) {
        return GLSL_KEYWORDS.contains(word);
    }

    /**
     * Check if a word is a GLSL swizzle.
     * @param word The word to check.
     * @return True if the word is a GLSL swizzle, false otherwise.
     */
    protected static boolean isSwizzle(String word) {
        return GLSL_SWIZZLES.contains(word);
    }

    /**
     * Check if a word is a GLSL built-in property.
     * @param word The word to check.
     * @return True if the word is a GLSL built-in property, false otherwise.
     */
    protected static boolean isBuiltinProperty(String word) {
        return GLSL_BUILTIN_PROPERTIES.contains(word);
    }

    /**
     * Check if a word is reserved (keyword, swizzle, or built-in property).
     * @param word The word to check.
     * @return True if the word is reserved, false otherwise.
     */
    public static boolean isReserved(String word) {
        return isKeyword(word) || isSwizzle(word) || isBuiltinProperty(word);
    }
}