package com.niwer;

import java.nio.file.Files;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

public class GlslObfuscatorPlugin implements Plugin<Project> {

    public static final String TASK_NAME = "obfuscateGlsl";

    @Override
    public void apply(Project project) {
        final GlslObfuscatorExtension EXTENSION = project.getExtensions().create(TASK_NAME, GlslObfuscatorExtension.class);

        project.getTasks().register(TASK_NAME, task -> {
            task.setGroup("glsl");
            task.setDescription("Obfuscates GLSL shader files from the configured source directory.");
            task.doLast(t -> obfuscateShaderFiles(project, EXTENSION.getSource()));
        });

        // Attach to processResources only when that task exists in the target project.
        project.getTasks().matching(task -> "processResources".equals(task.getName()))
            .configureEach(task -> task.doLast(t -> obfuscateShaderFiles(project, EXTENSION.getSource())));
    }

    private static void obfuscateShaderFiles(Project project, String sourceDir) {
        final String SOURCE_DIR = (sourceDir == null || sourceDir.isBlank()) ? "build" : sourceDir;

        final FileTree SHADER_FILES = project.fileTree(project.file(SOURCE_DIR)).matching(pattern -> pattern.include(
            "**/*.glsl",
            "**/*.vert", "**/*.frag",
            "**/*.fsh", "**/*.vsh"
        ));
        
        /* Traverse the shader files */
        SHADER_FILES.forEach(file -> {
            try {
                final String OBFUSCATED = GlslTask.obfuscate(file); // Obfuscate the GLSL file content
                Utils.print("Minifying GLSL file: " + file.getAbsolutePath()); // Log the file being processed
                Files.writeString(file.toPath(), OBFUSCATED);
            } catch (Exception e) {
                throw new RuntimeException("Error processing GLSL file: " + file.getAbsolutePath(), e);
            }
        });
    }
}