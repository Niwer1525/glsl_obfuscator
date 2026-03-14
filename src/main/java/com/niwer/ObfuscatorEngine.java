package com.niwer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObfuscatorEngine {

    public static String minify(List<String> lines) {
        final StringBuilder MINIFED_CODE = new StringBuilder();
        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing whitespace
            
            // Remove comments
            line = line.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "").trim();

            // Remove extra spaces
            line = line.replaceAll("\\s+", " ");

            // Add the minified line to the result
            if(!line.isEmpty()) MINIFED_CODE.append(line).append("\n");
        }
        return MINIFED_CODE.toString();
    }

    /* 
     * Should be the last called function to remove new lines and spaces
     */
    public static String removeNewLines(List<String> lines) {
        final StringBuilder MINIFED_CODE = new StringBuilder();
        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing whitespace

            if(line.startsWith("#")) {
                // Ensure preprocessor directives are always on their own line.
                // If previous content doesn't end with a newline, add one first.
                if (MINIFED_CODE.length() > 0 && MINIFED_CODE.charAt(MINIFED_CODE.length() - 1) != '\n')
                    MINIFED_CODE.append('\n');
                
                MINIFED_CODE.append(line).append("\n");
                continue; // Skip preprocessor directives
            }

            // Only append non-empty code lines and keep a single space between them
            if (!line.isEmpty()) MINIFED_CODE.append(line).append(" "); // Append the modified line to the result
        }
        return MINIFED_CODE.toString().trim(); // Remove trailing whitespace
    }

    public static String minifyVariableNames(List<String> lines) {
        final StringBuilder MINIFIED_CODE = new StringBuilder();
        final Set<String> UNIFORMS_NAMES = new HashSet<>(); // E.G : uScene
        final Map<String, String> VARIABLES = new HashMap<>(); // E.G : texCoord, v0
        int variableCount = 0;

        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing whitespace

            if(line.startsWith("#")) {
                MINIFIED_CODE.append(line).append("\n");
                continue; // Skip pre-processor directives
            }

            if(line.startsWith("uniform")) {
                // Find all uniform variable names using regex
                Pattern uniformPattern = Pattern.compile("uniform\\s+\\w+\\s+(\\w+);");
                Matcher uniformMatcher = uniformPattern.matcher(line);
                while (uniformMatcher.find()) {
                    UNIFORMS_NAMES.add(uniformMatcher.group(1)); // Add the uniform variable name to the set
                }
                MINIFIED_CODE.append(line).append("\n");
                continue; // Skip uniforms
            }

            if(line.startsWith("attribute") || line.startsWith("varying")) {
                // Treat attributes and varyings as uniforms for minification
                Pattern uniformPattern = Pattern.compile("(?:attribute|varying)\\s+\\w+\\s+(\\w+);");
                Matcher uniformMatcher = uniformPattern.matcher(line);
                while (uniformMatcher.find()) {
                    UNIFORMS_NAMES.add(uniformMatcher.group(1)); // Add the attribute/varying variable name to the set
                }
                MINIFIED_CODE.append(line).append("\n");
                continue; // Skip attributes and varyings
            }

            // Find declared variables (e.g: float a; vec3 b; mat4 c;)
            Pattern pattern = Pattern.compile("\\b(?:float|vec[234]|mat[234])\\s+(\\w+)");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String varName = matcher.group(1);
                if(!GlslVariables.isReserved(varName)) 
                    VARIABLES.put(varName, "v" + variableCount++); // Assign a minified name (e.g: v0, v1, ...)
            }

            // Generate a mapping for variable names to minified names
            for(Map.Entry<String, String> entry : VARIABLES.entrySet()) {
                String originalName = entry.getKey();
                String minifiedName = entry.getValue();
                if(UNIFORMS_NAMES.contains(originalName)) continue; // Skip uniforms
                
                // Use negative lookbehind to avoid replacing properties after . or ].
                // This prevents replacing built-in properties like diffuse in gl_FrontLightProduct[i].diffuse
                String replacementPattern = "(?<!\\.)(?<!\\]\\.)" + "\\b" + Pattern.quote(originalName) + "\\b";
                line = line.replaceAll(replacementPattern, minifiedName);
            }
            MINIFIED_CODE.append(line).append("\n"); // Append the modified line to the result
        }
        return MINIFIED_CODE.toString();
    }
}
