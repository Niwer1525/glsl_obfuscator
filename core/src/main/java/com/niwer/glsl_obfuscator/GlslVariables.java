package com.niwer.glsl_obfuscator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.niwer.glsl_obfuscator.utils.PatternUtils;
import com.niwer.glsl_obfuscator.utils.Utils;

public class GlslVariables {

    private GlslVariables() {}

    public static final Pattern KEEP_DIRECTIVE_PATTERN = Pattern.compile(
        "(?://|/\\*)\\s*@keep\\s+([\\w\\s,]+)"
    ); // This regex captures the @keep directive in comments, allowing multiple symbols to be specified (e.g., "// @keep a, b, c")

    private static final Set<String> PRECISION_QUALIFIERS = Set.of("lowp", "mediump", "highp", "precision"); // Precision qualifiers.
    private static final Set<String> STORAGE_QUALIFIERS = Set.of(
        "attribute", "const", "uniform", "varying",
        "in", "out", "inout",
        "centroid", "flat", "smooth", "noperspective", "invariant"
    );
    private static final Set<String> BASIC_TYPES = Set.of("void", "bool", "int", "uint", "float", "double"); // Basic types and scalars.

    /* Complex types and samplers */
    private static final Set<String> AGGREGATE_TYPES = Set.of(
        "vec2", "vec3", "vec4",
        "bvec2", "bvec3", "bvec4",
        "ivec2", "ivec3", "ivec4",
        "uvec2", "uvec3", "uvec4",
        "dvec2", "dvec3", "dvec4",
        "mat2", "mat3", "mat4",
        "mat2x2", "mat2x3", "mat2x4",
        "mat3x2", "mat3x3", "mat3x4",
        "mat4x2", "mat4x3", "mat4x4",
        "sampler1D", "sampler2D", "sampler3D", "samplerCube",
        "sampler1DShadow", "sampler2DShadow", "samplerCubeShadow"
    );

    private static final Set<String> TYPE_PATTERNS_FOR_REGEX = Utils.mergeSets(BASIC_TYPES, Set.of(
        "vec[234]", "u?ivec[234]", "bvec[234]", "dvec[234]",
        "mat[234](?:x[234])?",
        "sampler[123]D(?:Shadow)?", "samplerCube(?:Shadow)?"
    ));

    /*  keywords & control flow */
    private static final Set<String> CONTROL_KEYWORDS = Set.of(
        "break", "continue", "do", "else", "for", "if", "discard",
        "return", "switch", "case", "default", "while", "true", "false"
    );

    /* built-in functions */
    private static final Set<String> BUILTIN_IDENTIFIERS = Set.of(
        "main",
        "gl_FragColor", "gl_FragCoord", "gl_Position", "gl_VertexID", "gl_InstanceID",
        "gl_FragDepth", "gl_PointSize",
        "texture2D", "textureCube", "texture",
        "dot", "mix", "clamp", "fract", "sin", "cos", "tan", "asin", "acos", "atan",
        "pow", "exp", "log", "exp2", "log2", "sqrt", "inversesqrt",
        "abs", "sign", "floor", "ceil", "min", "max", "step", "smoothstep",
        "length", "distance", "cross", "normalize", "reflect", "refract"
    );
    
    public static final Pattern IO_PATTERN = PatternUtils.buildIoPattern(STORAGE_QUALIFIERS);
    public static final Pattern DECLARATION_PATTERN = PatternUtils.buildDeclarationPattern(Utils.mergeSets(PRECISION_QUALIFIERS, Set.of("const")), TYPE_PATTERNS_FOR_REGEX);
    
    public static final Set<String> GLSL_KEYWORDS = buildUnifiedKeywords();

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

    private static Set<String> buildUnifiedKeywords() {
        Set<String> keywords = new HashSet<>();
        keywords.addAll(PRECISION_QUALIFIERS);
        keywords.addAll(STORAGE_QUALIFIERS);
        keywords.addAll(BASIC_TYPES);
        keywords.addAll(AGGREGATE_TYPES);
        keywords.addAll(CONTROL_KEYWORDS);
        keywords.addAll(BUILTIN_IDENTIFIERS);
        return Collections.unmodifiableSet(keywords);
    }
}