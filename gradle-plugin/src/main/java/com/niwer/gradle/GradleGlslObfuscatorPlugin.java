package com.niwer.gradle;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

import com.niwer.GlslObfuscator;
import com.niwer.utils.Utils;

public class GradleGlslObfuscatorPlugin implements Plugin<Project> {

    public static final String TASK_NAME = "obfuscateGlsl";

    @Override
    public void apply(Project project) {
        final GradleGlslObfuscatorExtension EXTENSION = project.getExtensions().create(TASK_NAME, GradleGlslObfuscatorExtension.class);

        project.getTasks().register(TASK_NAME, task -> {
            task.setGroup("glsl");
            task.setDescription("Obfuscates GLSL shader files from the configured source directory.");
            task.doLast(t -> obfuscateShaderFiles(project, EXTENSION));
        });

        // Attach to processResources only when that task exists in the target project.
        project.getTasks().matching(task -> "processResources".equals(task.getName()))
            .configureEach(task -> task.doLast(t -> obfuscateShaderFiles(project, EXTENSION)));
    }

    private static void obfuscateShaderFiles(Project project, GradleGlslObfuscatorExtension extension) {
        final String SOURCE_DIR = extension.getSource();

        final FileTree SHADER_FILES = project.fileTree(project.file(SOURCE_DIR)).matching(pattern -> {
            /* Match all GLSL files */
            pattern.include(
                "**/*.glsl",
                "**/*.vert", "**/*.frag",
                "**/*.fsh", "**/*.vsh"
            );

            /* Exclude specified files */
            if (extension.getExcludedFiles() != null) pattern.exclude(extension.getExcludedFiles());
        });

        /* Process the shader files */
        final List<File> FILES_LIST = new ArrayList<>(SHADER_FILES.getFiles());
        if (FILES_LIST.isEmpty()) return;

        if (extension.isLinked()) {
            /* Process all files together (linked for #import or #include) */
            Utils.print("Obfuscating " + FILES_LIST.size() + " GLSL files with linked symbols...");
            Map<File, String> obfuscatedResults = GlslObfuscator.obfuscateProject(FILES_LIST, extension.shouldMinify(), extension.getExcludedSymbols() != null ? Set.copyOf(extension.getExcludedSymbols()) : Set.of());
            obfuscatedResults.forEach((file, content) -> {
                try {
                    Files.writeString(file.toPath(), content);
                } catch (Exception e) {
                    throw new RuntimeException("Error writing GLSL file: " + file.getAbsolutePath(), e);
                }
            });
        } else {
            /* One file at a time */
            for (File file : FILES_LIST) {
                try {
                    Utils.print("Minifying GLSL file: " + file.getAbsolutePath());
                    Files.writeString(file.toPath(), GlslObfuscator.obfuscate(file, extension.shouldMinify(), extension.getExcludedSymbols() != null ? Set.copyOf(extension.getExcludedSymbols()) : Set.of()));
                } catch (Exception e) {
                    throw new RuntimeException("Error processing GLSL file: " + file.getAbsolutePath(), e);
                }
            }
        }
    }
}