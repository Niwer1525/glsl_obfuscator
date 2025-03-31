package com.niwer;

public class GlslTaskTest {

    private static final String shaderCode = """
        #version 120

        uniform sampler2D uScene;
        uniform vec2 uResolution;
        uniform float uTime;
        uniform int uGogglesType;

        float random(vec2 st) {
            return fract(sin(dot(st, vec2(12.9898, 78.233))) * 23758.5453123); // 23758
        }

        void main() {
            vec2 texCoord = gl_FragCoord.xy/uResolution;
            vec4 sceneColor = texture2D(uScene, texCoord);
            
            vec2 uv = vec2(0.35 * sin(uTime * 10), 0.35 * cos(uTime * 10));
            vec3 noise = vec3(random(floor((texCoord + uv) * 250)) * 1.2); // 250 is the noise scale
            sceneColor.xy += noise.xy * 0.005;
            
            float intensity = dot(vec3(0.0, 0.29, 0.11), sceneColor.rgb);
            intensity = clamp(0.8 * intensity, 0, 1);
            
            float green = clamp(intensity / 0.2, 0, 1) * 40;
            vec4 greenColor = vec4(green * 0.7, green + 1, green * 0.7, 1);

            /* Goggles color */
            switch(uGogglesType) {
                case 0:
                    greenColor = vec4(0, 0, 0, 1);
                    break;
                case 1:
                    greenColor = vec4(greenColor.r, greenColor.g * 0.7, greenColor.b, 1);
                    break;
                case 2:
                    greenColor = vec4(greenColor.r, greenColor.g + 1 * 0.7, greenColor.b + 2.5, 1);
                    break;
            }

            float gray = dot(sceneColor.rgb, vec3(0.299, 0.587, 0.114));
            vec4 grayColor = vec4(gray, gray, gray, 1);

            sceneColor = grayColor * greenColor;
            gl_FragColor = vec4(sceneColor.rgb, 1);
        }
    """;

    private static void print(Object o) { System.out.println(o); }

    public static void main(String[] args) {
        String optimizedCode = GlslTask.obfuscate(GlslTask.getLines(shaderCode));
        print(optimizedCode);
    }

    // Yeah we should also make JUnit tests for this class, but we don't have time for that right now.
}