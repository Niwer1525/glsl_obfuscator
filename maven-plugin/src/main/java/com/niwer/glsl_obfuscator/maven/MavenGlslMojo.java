package com.niwer.glsl_obfuscator.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.niwer.glsl_obfuscator.GlslObfuscator;

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

    /**
     * Set to true to link symbols across shaders (#moj_import), or false for isolated files.
     */
    @Parameter(property = "glsl.linked", defaultValue = "true")
    private boolean linked;

    /**
     * Set to true to minify the GLSL code, or false to keep formatting.
     */
    @Parameter(property = "glsl.minify", defaultValue = "true")
    private boolean minify;

    @Parameter(property = "glsl.seed")
    private Long seed;

    /**
     * Set to true to obfuscate function and variable names separately, or false to treat them together.
     */
    @Parameter(property = "glsl.separateFuncsAndVars", defaultValue = "false")
    private boolean separateFuncsAndVars = false;

    /**
     * List of symbols that should not be obfuscated.
     */
    @Parameter(property = "glsl.excludedSymbols")
    private List<String> excludedSymbols;

    /**
     * List of files that should not be obfuscated.
     */
    @Parameter(property = "glsl.excludedFiles")
    private List<String> excludedFiles;

    private static final List<String> EXTENSIONS = List.of(
        ".glsl", ".vert", ".frag", ".fsh", ".vsh"
    );

    @Override
    public void execute() throws MojoExecutionException {
        if (!sourceDir.exists()) {
            getLog().warn("Source directory not found : " + sourceDir.getAbsolutePath());
            return;
        }

        List<File> shaderFiles;
        try (Stream<Path> paths = Files.walk(sourceDir.toPath())) {
            shaderFiles = paths.filter(Files::isRegularFile)
                               .filter(this::isShaderFile)
                               .map(Path::toFile)
                               .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Error during shader file collection", e);
        }

        if (shaderFiles.isEmpty()) {
            getLog().info("No shader files found in: " + sourceDir.getAbsolutePath());
            return;
        }

        if (linked) {
            /* Process all files together (linked for #import or #include) */
            getLog().info("Obfuscating " + shaderFiles.size() + " GLSL files with linked symbols...");
            try {
                final Map<File, String> RESULTS = GlslObfuscator.obfuscateProject(shaderFiles,
                    minify,
                    excludedSymbols != null ? new TreeSet<>(excludedSymbols) : Set.of(),
                    separateFuncsAndVars,
                    seed
                );
                for (Map.Entry<File, String> entry : RESULTS.entrySet()) Files.writeString(entry.getKey().toPath(), entry.getValue());
            } catch (Exception e) {
                throw new MojoExecutionException("Error during multi-file GLSL obfuscation", e);
            }
        } else {
            /* One file at a time */
            for (final File FILE : shaderFiles) {
                try {
                    getLog().info("Obfuscating GLSL file: " + FILE.getAbsolutePath());
                    String obfuscated = GlslObfuscator.obfuscate(FILE,
                        minify, excludedSymbols != null ? new TreeSet<>(excludedSymbols) : Set.of(),
                        separateFuncsAndVars,
                        seed
                    );
                    Files.writeString(FILE.toPath(), obfuscated);
                } catch (Exception e) {
                    getLog().error("Error processing file " + FILE.getAbsolutePath(), e);
                }
            }
        }
    }

    private boolean isShaderFile(Path path) {
        final String NAME = path.getFileName().toString().toLowerCase();
        return EXTENSIONS.stream().anyMatch(NAME::endsWith);
    }
}