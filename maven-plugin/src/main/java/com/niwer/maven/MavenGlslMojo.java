package com.niwer.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.niwer.GlslTask;

/**
 * Attaches to the "process-resources" phase of the Maven build lifecycle and obfuscates GLSL shader files.
 * 
 * @author Niwer
 */
@Mojo(name = "obfuscate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MavenGlslMojo extends AbstractMojo {

    /**
     * Configurable source directory for GLSL files, defaults to the build output directory.
     */
    @Parameter(property = "glsl.sourceDir", defaultValue = "${project.build.outputDirectory}")
    private File sourceDir;

    private static final List<String> EXTENSIONS = List.of(
        ".glsl", ".vert", ".frag", ".fsh", ".vsh"
    );

    @Override
    public void execute() throws MojoExecutionException {
        if (!sourceDir.exists()) {
            getLog().warn("Dossier introuvable : " + sourceDir.getAbsolutePath());
            return;
        }

        try (Stream<Path> paths = Files.walk(sourceDir.toPath())) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isShaderFile)
                 .forEach(this::processFile);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during shader file processing", e);
        }
    }

    private boolean isShaderFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private void processFile(Path path) {
        try {
            getLog().info("Minifying GLSL file: " + path);
            String obfuscated = GlslTask.obfuscate(path.toFile());
            Files.writeString(path, obfuscated);
        } catch (Exception e) {
            getLog().error("Error processing file " + path, e);
        }
    }
}