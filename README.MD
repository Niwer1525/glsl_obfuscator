# GLSL Obfuscator
## This will minify and obfuscate your GLSL shaders.

# How it works
Exemple shader (You'll see conversion step by step):
```glsl
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
```

First it will remove all spaces, empty lines and comments from the script.
```glsl	
#version 120
uniform sampler2D uScene;
uniform vec2 uResolution;
uniform float uTime;
uniform int uGogglesType;
float random(vec2 st) {
return fract(sin(dot(st, vec2(12.9898, 78.233))) * 23758.5453123);
}
void main() {
vec2 texCoord = gl_FragCoord.xy/uResolution;
vec4 sceneColor = texture2D(uScene, texCoord);
vec2 uv = vec2(0.35 * sin(uTime * 10), 0.35 * cos(uTime * 10));
vec3 noise = vec3(random(floor((texCoord + uv) * 250)) * 1.2);
sceneColor.xy += noise.xy * 0.005;
float intensity = dot(vec3(0.0, 0.29, 0.11), sceneColor.rgb);
intensity = clamp(0.8 * intensity, 0, 1);
float green = clamp(intensity / 0.2, 0, 1) * 40;
vec4 greenColor = vec4(green * 0.7, green + 1, green * 0.7, 1);
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
```
Then it will replace all variables names with random names.
```glsl
#version 120

uniform sampler2D uScene;
uniform vec2 uResolution;
uniform float uTime;
uniform int uGogglesType;

float v0(vec2 st) {
return fract(sin(dot(st, vec2(12.9898, 78.233))) * 23758.5453123); // 23758
}

void main() {
vec2 v1 = gl_FragCoord.xy/uResolution;
vec4 v2 = texture2D(uScene, v1);

vec2 v3 = vec2(0.35 * sin(uTime * 10), 0.35 * cos(uTime * 10));
vec3 v4 = vec3(v0(floor((v1 + v3) * 250)) * 1.2); // 250 is the v4 scale
v2.xy += v4.xy * 0.005;

float v5 = dot(vec3(0.0, 0.29, 0.11), v2.rgb);
v5 = clamp(0.8 * v5, 0, 1);

float v6 = clamp(v5 / 0.2, 0, 1) * 40;
vec4 v7 = vec4(v6 * 0.7, v6 + 1, v6 * 0.7, 1);

/* Goggles color */
switch(uGogglesType) {
case 0:
v7 = vec4(0, 0, 0, 1);
break;
case 1:
v7 = vec4(v7.r, v7.g * 0.7, v7.b, 1);
break;
case 2:
v7 = vec4(v7.r, v7.g + 1 * 0.7, v7.b + 2.5, 1);
break;
}

float v8 = dot(v2.rgb, vec3(0.299, 0.587, 0.114));
vec4 v9 = vec4(v8, v8, v8, 1);

v2 = v9 * v7;
gl_FragColor = vec4(v2.rgb, 1);
}
```
Finally it will remove all the new lines between the code.
```glsl
#version 120
uniform sampler2D uScene; uniform vec2 uResolution; uniform float uTime; uniform int uGogglesType; float v0(vec2 st) { return fract(sin(dot(st, vec2(12.9898, 78.233))) * 23758.5453123); } void main() { vec2 v1 = gl_FragCoord.xy/uResolution; vec4 v2 = texture2D(uScene, v1); vec2 v3 = vec2(0.35 * sin(uTime * 10), 0.35 * cos(uTime * 10)); vec3 v4 = vec3(v0(floor((v1 + v3) * 250)) * 1.2); v2.xy += v4.xy * 0.005; float v5 = dot(vec3(0.0, 0.29, 0.11), v2.rgb); v5 = clamp(0.8 * v5, 0, 1); float v6 = clamp(v5 / 0.2, 0, 1) * 40; vec4 v7 = vec4(v6 * 0.7, v6 + 1, v6 * 0.7, 1); switch(uGogglesType) { case 0: v7 = vec4(0, 0, 0, 1); break; case 1: v7 = vec4(v7.r, v7.g * 0.7, v7.b, 1); break; case 2: v7 = vec4(v7.r, v7.g + 1 * 0.7, v7.b + 2.5, 1); break; } float v8 = dot(v2.rgb, vec3(0.299, 0.587, 0.114)); vec4 v9 = vec4(v8, v8, v8, 1); v2 = v9 * v7; gl_FragColor = vec4(v2.rgb, 1); }
```

Of course it will keep pre-processors lines (#version, #extension, #define, #include, etc.) and it will keep the main function name (main) and the gl_FragColor variable name (gl_FragColor).

# Usage
## Gradle
This is the most recommended way to use the shader minifier. You can add the following code to your build.gradle file:
```groovy
buildscript {
	dependencies {
		classpath fileTree(dir: 'libs', include: ['*.jar'])
	}
}

apply plugin: 'com.niwer.glsl_obfuscator'
```
You also need to put the JAR file into your project's "libs/" folder.
- Project
    - libs
        - glsl-obfuscator.jar
    - build.gradle
Then all files ending with : 
    - .glsl
    - .vert
    - .frag
    - .fsh
    - .vsh
Will be minified and obfuscated automatically when you build your project. (This happens in the "processResources" task.)

## Python (Manually convert one file)
You only need to execute the "shader_minifier.py" script. The script will prompt you for the input file. The script will read the input file, minify/obfsucate it, and print the output into the console.

Make sure you have Python installed on your system. You can download it from [python.org](https://www.python.org/downloads/).
```bash
python shader_minifier.py
```
