using Niwer.GlslObfuscator;
using System;
using System.IO;
using Xunit;

namespace Niwer.GlslObfuscator.Tests;

public class GlslObfuscatorTests
{
    private const string Shader = @"
        #version 120
        uniform vec2 uResolution;
        float brightness = 1.0;
        void main() {
            brightness = brightness + uResolution.x;
        }";

    [Fact]
    public void ObfuscatesDeclarationsAndReferences()
    {
        var result = GlslObfuscator.Obfuscate("float brightness = 1.0;\nvoid main() { brightness = brightness + 1.0; }");
        Assert.Contains("float o0", result);
        Assert.DoesNotContain("brightness", result);
    }

    [Fact]
    public void KeepsExcludedAndAnnotatedSymbols()
    {
        var result = GlslObfuscator.Obfuscate("// @keep brightness\nfloat brightness = 1.0;\nfloat other = brightness;", excludedSymbols: new[] { "other" });
        Assert.Contains("brightness", result);
        Assert.Contains("other", result);
    }

    [Fact]
    public void SeedIsRepeatable()
    {
        const string shader = "float brightness = 1.0; void main() { brightness += 1.0; }";
        Assert.Equal(GlslObfuscator.Obfuscate(shader, seed: 42), GlslObfuscator.Obfuscate(shader, seed: 42));
    }

    [Fact]
    public void NullAndEmptyInputsMatchJavaApiBehavior()
    {
        Assert.Throws<ArgumentNullException>(() => GlslObfuscator.Obfuscate(null!));
        Assert.Throws<ArgumentNullException>(() => GlslObfuscator.ObfuscateLines(null!));
        Assert.Equal(string.Empty, GlslObfuscator.Obfuscate(string.Empty));
        Assert.Equal(string.Empty, GlslObfuscator.ObfuscateLines(Array.Empty<string>()));
        Assert.Empty(GlslObfuscator.ObfuscateProject(Array.Empty<string>()));
        Assert.Throws<ArgumentNullException>(() => GlslObfuscator.ObfuscateProject(null!));
    }

    [Fact]
    public void ObfuscatesFromLinesWithAndWithoutMinification()
    {
        var lines = Shader.Split('\n');
        var minified = GlslObfuscator.ObfuscateLines(lines, minify: true);
        var formatted = GlslObfuscator.ObfuscateLines(lines, minify: false);

        Assert.NotEmpty(minified);
        Assert.NotEmpty(formatted);
        Assert.DoesNotContain("//", minified);
        Assert.Contains("\n", formatted);
    }

    [Fact]
    public void PreservesReservedInterfaceAndProperties()
    {
        var result = GlslObfuscator.Obfuscate(Shader);

        Assert.Contains("uResolution", result);
        Assert.Contains("uResolution.x", result);
        Assert.DoesNotContain("brightness", result);
    }

    [Fact]
    public void SeparateFunctionsAndVariablesUseDifferentPrefixes()
    {
        var result = GlslObfuscator.Obfuscate(
            "float calculate(float inputValue) { float localValue = inputValue; return localValue; } void main() { calculate(1.0); }",
            separateFunctionsAndVariables: true);

        Assert.Contains("f", result);
        Assert.Contains("v", result);
        Assert.DoesNotContain("calculate", result);
        Assert.DoesNotContain("localValue", result);
    }

    [Fact]
    public void ObfuscatesFromFileAndRejectsMissingFile()
    {
        var path = Path.Combine(Path.GetTempPath(), "glsl-obfuscator-" + Guid.NewGuid() + ".frag");
        try
        {
            File.WriteAllText(path, Shader);
            Assert.NotEmpty(GlslObfuscator.ObfuscateFile(path));
            Assert.Throws<FileNotFoundException>(() => GlslObfuscator.ObfuscateFile(path + ".missing"));
            Assert.Throws<ArgumentNullException>(() => GlslObfuscator.ObfuscateFile(null!));
        }
        finally
        {
            if (File.Exists(path)) File.Delete(path);
        }
    }

    [Fact]
    public void ObfuscatesProjectWithSharedSymbols()
    {
        var directory = Path.Combine(Path.GetTempPath(), "glsl-obfuscator-" + Guid.NewGuid());
        Directory.CreateDirectory(directory);
        var first = Path.Combine(directory, "main.glsl");
        var second = Path.Combine(directory, "shared.glsl");
        try
        {
            File.WriteAllText(first, "void main() { sharedValue = 1.0; }");
            File.WriteAllText(second, "float sharedValue = 0.0;");

            var result = GlslObfuscator.ObfuscateProject(new[] { first, second }, seed: 12345);

            Assert.Equal(2, result.Count);
            Assert.DoesNotContain("sharedValue", result[first]);
            Assert.DoesNotContain("sharedValue", result[second]);
            Assert.Throws<FileNotFoundException>(() =>
                GlslObfuscator.ObfuscateProject(new[] { first + ".missing" }));
        }
        finally
        {
            if (Directory.Exists(directory)) Directory.Delete(directory, recursive: true);
        }
    }
}
