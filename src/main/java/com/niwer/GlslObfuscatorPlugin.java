package com.niwer;

import java.nio.file.Files;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

public class GlslObfuscatorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().named("processResources", task -> {
            task.doLast(t -> {
                FileTree glslFiles = project.fileTree(project.getLayout().getBuildDirectory()).matching(pattern -> {
                    pattern.include(
                        "**/*.glsl",
                        "**/*.vert", "**/*.frag",
                        "**/*.fsh", "**/*.vsh"
                    );
                });
                
                glslFiles.forEach(file -> {
                    try {
                        String minifiedCode = GlslTask.obfuscate(file); // Obfusquer le code GLSL
                        System.out.println("Minifying GLSL file: " + file.getAbsolutePath());
                        Files.writeString(file.toPath(), minifiedCode);
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing GLSL file: " + file.getAbsolutePath(), e);
                    }
                });
            });
        });
    }
}