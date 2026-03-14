package com.niwer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlslObfuscatorPluginTest {

    @TempDir
    File testProjectDir;
    
    private static File createBuildFile(File projectDir, String content) throws Exception {
        final File BUILD_FILE = new File(projectDir, "build.gradle");
        Files.writeString(BUILD_FILE.toPath(), content);
        return BUILD_FILE;
    }

    private static File createShaderFile(File shaderDir) throws Exception {
        if (!shaderDir.exists()) shaderDir.mkdirs();

        final File SHADER_FILE = new File(shaderDir, "sample.frag");
        Files.writeString(SHADER_FILE.toPath(), "void main(){ gl_FragColor = vec4(1.0); }");
        return SHADER_FILE;
    }

    @Test void testPluginApplicationWithoutProcessResourcesTask() throws Exception {
        // Testing this with JUnit is a bit tricky since it requires a Gradle project context.
        // We have to use Gradle TestKit to create a temporary project and apply the plugin to it,
        // then verify the expected behavior.

        final String FILE_CONTENT = String.format("""
            plugins { id '%s' }\n
            %s { source = 'src/main/glsl' }
        """,
        "com.niwer.glsl_obfuscator", "obfuscateGlsl");
        createBuildFile(testProjectDir, FILE_CONTENT);
        createShaderFile(new File(testProjectDir, "src/main/glsl")); // Create a sample shader file to be obfuscated

        final BuildResult RESULT = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withGradleVersion("9.1.0") // Because of the Java 21/25 compatibility, we need to use Gradle 9.1 or higher
            .withPluginClasspath() // It will automatically include the plugin from the classpath
            .withArguments(GlslObfuscatorPlugin.TASK_NAME) // Our custom task to test
            .build();

        assertTrue(RESULT.getOutput().contains("SUCCESS"), "GLSL obfuscation task should run successfully");
    }

    @Test void testPluginApplicationWithProcessResourcesTask() throws Exception {
        // Similar to the previous test, but this time we create a Java project with a processResources task
        // and verify that the obfuscation logic is executed.

        final String FILE_CONTENT = String.format("""
            plugins {
                id 'java'
                id '%s'
            }
        """, "com.niwer.glsl_obfuscator");
        createBuildFile(testProjectDir, FILE_CONTENT);
        createShaderFile(new File(testProjectDir, "src/main/resources")); // Create a sample shader file to be obfuscated

        final BuildResult RESULT = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withGradleVersion("9.1.0")
            .withPluginClasspath()
            .withArguments("processResources") // We run the processResources task which should trigger our obfuscation
            .build();

        assertTrue(RESULT.getOutput().contains("Minifying GLSL file"), "GLSL obfuscation should be triggered during processResources");
    }
}
