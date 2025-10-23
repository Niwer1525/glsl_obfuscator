package com.niwer;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlslTask {

    /* Constants */
    private static final Set<String> GLSL_KEYWORDS = Set.of(
        "attribute", "const", "uniform", "varying", "break", "continue",
        "do", "else", "for", "if", "discard", "return", "switch",
        "case", "default", "in", "out", "inout", "float", "int",
        "void", "bool", "true", "false", "mat2", "mat3",
        "mat4", "vec2", "vec3", "vec4", "while",
        "sampler2D", "samplerCube", "main",
        "gl_FragColor", "gl_FragCoord", "gl_Position", "gl_VertexID",
        "texture2D", "textureCube", "dot", "mix", "clamp", "fract", "sin", "cos"
    );
    private static final Set<String> GLSL_SWIZZLES = Set.of("x", "y", "z", "w", "r", "g", "b", "a", "xy", "xz", "yz", "rgb", "rgba", "st", "stp");
    private static final Set<String> GLSL_BUILTIN_PROPERTIES = Set.of(
        "diffuse", "ambient", "specular", "position", "spotDirection", "spotExponent",
        "spotCutoff", "constantAttenuation", "linearAttenuation", "quadraticAttenuation"
    );


    public static String obfuscate(File file) {
        if (file == null) throw new RuntimeException("File is null");
        if (!file.exists()) throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        try {
            return obfuscate(Files.readAllLines(file.toPath())); // Read the file and minify its content
        } catch (Exception e) { throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e); }
    }

    public static String obfuscate(String content) {
        if (content == null) throw new RuntimeException("Content is null");
        return obfuscate(getLines(content)); // Split the content into lines and minify it
    }

    public static String obfuscate(List<String> content) {
        final List<String> MINIFED_LINES = getLines(minifyGlsl(content)); // Minify the GLSL code
        final List<String> MNIFIED_VARIABLES = getLines(minifyVariableNames(MINIFED_LINES)); // Minify variable names

        return removeNewLines(MNIFIED_VARIABLES); // Remove new lines
    }

    public static List<String> getLines(String shaderCode) {
        return List.of(shaderCode.split("\n")); // Split the shader code into lines
    }

    public static String minifyGlsl(List<String> lines) {
        final StringBuilder minifiedCode = new StringBuilder();
        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing whitespace
            
            // Remove comments
            line = line.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "").trim();

            // Remove extra spaces
            line = line.replaceAll("\\s+", " ");

            // Add the minified line to the result
            if(!line.isEmpty()) minifiedCode.append(line).append("\n");
        }
        return minifiedCode.toString();
    }

    /* 
     * Should be the last called function to remove new lines and spaces
     */
    public static String removeNewLines(List<String> lines) {
        final StringBuilder minifiedCode = new StringBuilder();
        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing whitespace
            if(line.startsWith("#")) {
                // Ensure preprocessor directives are always on their own line.
                // If previous content doesn't end with a newline, add one first.
                if (minifiedCode.length() > 0 && minifiedCode.charAt(minifiedCode.length() - 1) != '\n')
                    minifiedCode.append('\n');
                
                minifiedCode.append(line).append("\n");
                continue; // Skip preprocessor directives
            }
            // Only append non-empty code lines and keep a single space between them
            if (!line.isEmpty()) minifiedCode.append(line).append(" "); // Append the modified line to the result
        }
        return minifiedCode.toString().trim(); // Remove trailing whitespace
    }

    public static String minifyVariableNames(List<String> lines) {
        final StringBuilder minifiedCode = new StringBuilder();
        final Set<String> uniformsNames = new HashSet<>(); // E.G : uScene
        final Map<String, String> variables = new HashMap<>(); // E.G : texCoord, v0
        int variableCount = 0;

        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing whitespace
            if(line.startsWith("#")) {
                minifiedCode.append(line).append("\n");
                continue; // Skip preprocessor directives
            }

            if(line.startsWith("uniform")) {
                // Find all uniform variable names using regex
                Pattern uniformPattern = Pattern.compile("uniform\\s+\\w+\\s+(\\w+);");
                Matcher uniformMatcher = uniformPattern.matcher(line);
                while (uniformMatcher.find()) {
                    uniformsNames.add(uniformMatcher.group(1)); // Add the uniform variable name to the set
                }
                minifiedCode.append(line).append("\n");
                continue; // Skip uniforms
            }

            if(line.startsWith("attribute") || line.startsWith("varying")) {
                // Treat attributes and varyings as uniforms for minification
                Pattern uniformPattern = Pattern.compile("(?:attribute|varying)\\s+\\w+\\s+(\\w+);");
                Matcher uniformMatcher = uniformPattern.matcher(line);
                while (uniformMatcher.find()) {
                    uniformsNames.add(uniformMatcher.group(1)); // Add the attribute/varying variable name to the set
                }
                minifiedCode.append(line).append("\n");
                continue; // Skip attributes and varyings
            }

            // Find declared variables (e.g: float a; vec3 b; mat4 c;)
            Pattern pattern = Pattern.compile("\\b(?:float|vec[234]|mat[234])\\s+(\\w+)");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String varName = matcher.group(1);
                if(!GLSL_KEYWORDS.contains(varName) && !GLSL_SWIZZLES.contains(varName) && !GLSL_BUILTIN_PROPERTIES.contains(varName)) 
                    variables.put(varName, "v" + variableCount++); // Assign a minified name (e.g: v0, v1, ...)
            }

            // Generate a mapping for variable names to minified names
            for(Map.Entry<String, String> entry : variables.entrySet()) {
                String originalName = entry.getKey();
                String minifiedName = entry.getValue();
                if(uniformsNames.contains(originalName)) continue; // Skip uniforms
                
                // Use negative lookbehind to avoid replacing properties after . or ].
                // This prevents replacing built-in properties like diffuse in gl_FrontLightProduct[i].diffuse
                String replacementPattern = "(?<!\\.)(?<!\\]\\.)" + "\\b" + Pattern.quote(originalName) + "\\b";
                line = line.replaceAll(replacementPattern, minifiedName);
            }
            minifiedCode.append(line).append("\n"); // Append the modified line to the result
        }
        return minifiedCode.toString();
    }
}
