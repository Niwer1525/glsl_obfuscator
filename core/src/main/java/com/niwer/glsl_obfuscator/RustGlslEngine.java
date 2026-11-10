package com.niwer.glsl_obfuscator;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.util.Collection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

/** Java binding for the shared Rust obfuscation engine. */
final class RustGlslEngine implements AutoCloseable {
    private interface NativeApi extends Library {
        Pointer glsl_context_new(String excludedCsv, byte separate, long seed);
        byte glsl_context_collect(Pointer context, String source);
        Pointer glsl_context_transform(Pointer context, String source, byte minify);
        void glsl_context_free(Pointer context);
        void glsl_string_free(Pointer value);
    }

    private static final NativeApi API = loadNativeApi();
    private Pointer context;

    private static NativeApi loadNativeApi() {
        String mappedName = System.mapLibraryName("glsl_obfuscator_core");
        String platform = platformDirectory();
        String resource = "/META-INF/native/" + platform + "/" + mappedName;
        try (InputStream input = RustGlslEngine.class.getResourceAsStream(resource)) {
            if (input == null) {
                return Native.load("glsl_obfuscator_core", NativeApi.class);
            }
            File extracted = Files.createTempFile("glsl-obfuscator-", "-" + mappedName).toFile();
            extracted.deleteOnExit();
            Files.copy(input, extracted.toPath(), StandardCopyOption.REPLACE_EXISTING);
            extracted.setExecutable(true);
            return Native.load(extracted.getAbsolutePath(), NativeApi.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to extract the Rust obfuscator native library.", exception);
        }
    }

    private static String platformDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String architecture = System.getProperty("os.arch").toLowerCase();
        String normalizedArchitecture = architecture.contains("aarch64") || architecture.contains("arm64")
            ? "arm64" : "x64";
        if (os.contains("win")) return "win-" + normalizedArchitecture;
        if (os.contains("mac")) return "osx-" + normalizedArchitecture;
        return "linux-" + normalizedArchitecture;
    }

    private RustGlslEngine(Collection<String> excluded, boolean separate, Long seed) {
        String excludedCsv = excluded == null ? "" : excluded.stream().collect(Collectors.joining("\n"));
        context = API.glsl_context_new(excludedCsv, (byte) (separate ? 1 : 0), seed == null ? -1 : seed);
        if (context == null) throw new IllegalStateException("The Rust obfuscator context could not be created.");
    }

    static RustGlslEngine create(Collection<String> excluded, boolean separate, Long seed) {
        return new RustGlslEngine(excluded, separate, seed);
    }

    void collect(String source) {
        if (API.glsl_context_collect(context, source) == 0) {
            throw new IllegalStateException("The Rust obfuscator could not collect shader symbols.");
        }
    }

    String transform(String source, boolean minify) {
        Pointer result = API.glsl_context_transform(context, source, (byte) (minify ? 1 : 0));
        if (result == null) throw new IllegalStateException("The Rust obfuscator could not transform shader source.");
        try {
            return result.getString(0, "UTF-8");
        } finally {
            API.glsl_string_free(result);
        }
    }

    @Override
    public void close() {
        if (context != null) {
            API.glsl_context_free(context);
            context = null;
        }
    }
}
