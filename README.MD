# GLSL Obfuscator
A minifier and obfuscator for GLSL shaders, supporting standalone files as well as multi-file projects with preprocessor imports (such as Minecraft's `#moj_import`).

> Available as a **Java Core Library**, a **Gradle Plugin**, and a **Maven Plugin**.

## Overview

### Before
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
    vec2 texCoord = gl_FragCoord.xy / uResolution;
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

### After
```glsl
#version 120
uniform sampler2D uScene; uniform vec2 uResolution; uniform float uTime; uniform int uGogglesType; float v0(vec2 st) { return fract(sin(dot(st, vec2(12.9898, 78.233))) * 23758.5453123); } void main() { vec2 v1 = gl_FragCoord.xy/uResolution; vec4 v2 = texture2D(uScene, v1); vec2 v3 = vec2(0.35 * sin(uTime * 10), 0.35 * cos(uTime * 10)); vec3 v4 = vec3(v0(floor((v1 + v3) * 250)) * 1.2); v2.xy += v4.xy * 0.005; float v5 = dot(vec3(0.0, 0.29, 0.11), v2.rgb); v5 = clamp(0.8 * v5, 0, 1); float v6 = clamp(v5 / 0.2, 0, 1) * 40; vec4 v7 = vec4(v6 * 0.7, v6 + 1, v6 * 0.7, 1); switch(uGogglesType) { case 0: v7 = vec4(0, 0, 0, 1); break; case 1: v7 = vec4(v7.r, v7.g * 0.7, v7.b, 1); break; case 2: v7 = vec4(v7.r, v7.g + 1 * 0.7, v7.b + 2.5, 1); break; } float v8 = dot(v2.rgb, vec3(0.299, 0.587, 0.114)); vec4 v9 = vec4(v8, v8, v8, 1); v2 = v9 * v7; gl_FragColor = vec4(v2.rgb, 1); }
```

## Installation & Usage

### Supported File Extensions
- `.glsl`
- `.frag`
- `.vert`
- `.fsh`
- `.vsh`

### Gradle Plugin

```groovy
buildscript {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.Niwer1525.glsl_obfuscator:glsl-obfuscator-gradle-plugin:v1.0.7'
    }
}

apply plugin: 'com.niwer.glsl_obfuscator'
```

> **Note**: By default, the plugin processes files from the configured directory (default: build/resources/main). You can specify a custom source directory:

```groovy
obfuscateGlsl {
    source = 'src/main/resources/assets/shaders'
    linked = true // Enable multi-file obfuscation for projects with #import, #include or #moj_import directives
    minify = true // Set to false to keep formatting
    seed = 12345L // Optional seed for deterministic obfuscation
    separateFuncsAndVars = true // Set to true to obfuscate function and variable names separately
    excludedSymbols = ['func123', 'mySpecialVar', 'matrixCalculator'] // Symbols that should not be obfuscated
    excludedFiles = ['**/ignored_shader.glsl', '**/debug/**'] // Files that should not be obfuscated
}
```

The obfuscation task automatically hooks into processResources, and can also be run independently `./gradlew obfuscateGlsl`

### Maven Plugin

```xml
<pluginRepositories>
    <pluginRepository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </pluginRepository>
</pluginRepositories>
```

```xml
<plugin>
    <groupId>com.github.Niwer1525.glsl_obfuscator</groupId>
    <artifactId>glsl-obfuscator-maven-plugin</artifactId>
    <version>1.0.7</version>
    <executions>
        <execution>
            <phase>process-resources</phase>
            <goals>
                <goal>obfuscate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <sourceDir>${project.build.outputDirectory}</sourceDir>
        <linked>true</linked> <!-- Enable multi-file obfuscation for projects with #import, #include or #moj_import directives -->
        <minify>true</minify> <!-- Set to false to keep formatting -->
        <seed>12345</seed> <!-- Optional seed for deterministic obfuscation -->
        <separateFuncsAndVars>true</separateFuncsAndVars> <!-- Set to true to obfuscate function and variable names separately -->
        <!-- List of symbols that should not be obfuscated -->
        <excludedSymbols>
            <symbol>func123</symbol>
            <symbol>myGlobalMatrix</symbol>
        </excludedSymbols>
        <!-- List of files that should not be obfuscated -->
        <excludedFiles>
            <pattern>**/dynamic_*.glsl</pattern>
        </excludedFiles>
    </configuration>
</plugin>
```

### Standalone / Java API

Gradle Dependency:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
	implementation 'com.github.Niwer1525.glsl_obfuscator:glsl-obfuscator-core:v1.0.7'
}
```

Maven Dependency:
```xml
<dependency>
	<groupId>com.github.Niwer1525.glsl_obfuscator</groupId>
	<artifactId>glsl-obfuscator-core</artifactId>
	<version>v1.0.7</version>
</dependency>
```

Code Usage:
```java
import com.niwer.GlslTask;
import java.io.File;
import java.util.List;
import java.util.Map;

// Single file obfuscation & minification
String singleResult = GlslTask.obfuscate(new File("path/to/shader.fsh"), true, true, 12345L);

// Multi-file obfuscation & minification with synchronized symbol maps
Map<File, String> projectResults = GlslTask.obfuscateProject(List.of(
    new File("shaders/main.fsh"),
    new File("shaders/math.glsl"),
    new File("shaders/model_calculator.glsl")
), true, true, 12345L);
```