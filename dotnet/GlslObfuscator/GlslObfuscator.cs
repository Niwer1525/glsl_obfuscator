using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace Niwer.GlslObfuscator;

/// <summary>Minifies and obfuscates GLSL shader source.</summary>
public static class GlslObfuscator
{
    /// <summary>
    /// Obfuscates shader source supplied as one string.
    /// </summary>
    /// <param name="content">The GLSL source to process.</param>
    /// <param name="minify">When true, removes comments, excess whitespace, and unnecessary newlines.</param>
    /// <param name="separateFunctionsAndVariables">When true, functions use <c>f</c> names and variables use <c>v</c> names.</param>
    /// <param name="seed">An optional seed for repeatable generated names.</param>
    /// <param name="excludedSymbols">Symbols that must remain unchanged.</param>
    /// <returns>The processed GLSL source.</returns>
    public static string Obfuscate(string content, bool minify = true, bool separateFunctionsAndVariables = false,
        long? seed = null, IEnumerable<string>? excludedSymbols = null)
    {
        if (content is null) throw new ArgumentNullException(nameof(content));
        return ObfuscateLines(content.Length == 0 ? Array.Empty<string>() : Lines(content), minify, excludedSymbols, separateFunctionsAndVariables, seed);
    }

    /// <summary>
    /// Reads and obfuscates a shader file without modifying the input file.
    /// </summary>
    public static string ObfuscateFile(string filePath, bool minify = true, bool separateFunctionsAndVariables = false,
        long? seed = null, IEnumerable<string>? excludedSymbols = null)
    {
        if (filePath is null) throw new ArgumentNullException(nameof(filePath));
        if (!File.Exists(filePath)) throw new FileNotFoundException("GLSL file does not exist.", filePath);
        return ObfuscateLines(File.ReadAllLines(filePath), minify, excludedSymbols, separateFunctionsAndVariables, seed);
    }

    /// <summary>
    /// Reads and obfuscates multiple shader files using one shared symbol table.
    /// Files are discovered in the supplied order but symbols are collected by file name,
    /// making project output independent of the input enumeration order.
    /// </summary>
    public static IDictionary<string, string> ObfuscateProject(IEnumerable<string> filePaths, bool minify = true,
        bool separateFunctionsAndVariables = false, long? seed = null, IEnumerable<string>? excludedSymbols = null)
    {
        if (filePaths is null) throw new ArgumentNullException(nameof(filePaths));
        var files = filePaths.ToList();
        if (files.Count == 0) return new Dictionary<string, string>();
        var context = new ObfuscationContext(excludedSymbols ?? Enumerable.Empty<string>(), separateFunctionsAndVariables, seed);
        var cleaned = new List<(string Path, List<string> Lines)>();
        foreach (var path in files.OrderBy(Path.GetFileName, StringComparer.Ordinal))
        {
            if (!File.Exists(path)) throw new FileNotFoundException("GLSL file does not exist.", path);
            var lines = File.ReadAllLines(path).ToList();
            Engine.CollectSymbols(lines, context);
            cleaned.Add((path, Lines(Engine.ClearComments(lines, minify))));
        }

        var result = new Dictionary<string, string>();
        foreach (var item in cleaned)
        {
            var output = Engine.ApplyObfuscation(item.Lines, context, minify);
            result[item.Path] = minify ? Engine.RemoveNewLines(output) : output;
        }
        return result;
    }

    /// <summary>
    /// Obfuscates source represented as lines. This overload is useful when the caller
    /// already has source loaded and wants to avoid joining and splitting it again.
    /// </summary>
    public static string ObfuscateLines(IEnumerable<string> lines, bool minify = true,
        IEnumerable<string>? excludedSymbols = null, bool separateFunctionsAndVariables = false, long? seed = null)
    {
        if (lines is null) throw new ArgumentNullException(nameof(lines));
        var source = lines.ToList();
        if (source.Count == 0) return string.Empty;
        var context = new ObfuscationContext(excludedSymbols ?? Enumerable.Empty<string>(), separateFunctionsAndVariables, seed);
        Engine.CollectSymbols(source, context);
        var cleaned = Lines(Engine.ClearComments(source, minify));
        var output = Engine.ApplyObfuscation(cleaned, context, minify);
        return minify ? Engine.RemoveNewLines(output) : output;
    }

    // Keep empty entries so that non-minified output preserves blank lines.
    private static List<string> Lines(string text) => text.Split(new[] { "\n" }, StringSplitOptions.None).ToList();
}

internal sealed class ObfuscationContext
{
    internal readonly Dictionary<string, string> Symbols = new();
    internal readonly HashSet<string> Blacklist = new();
    private readonly bool separate;
    private readonly List<int>? shuffled;
    private int globalCounter, variableCounter, functionCounter;

    internal ObfuscationContext(IEnumerable<string>? initial, bool separateFunctionsAndVariables, long? seed)
    {
        if (initial != null) foreach (var item in initial) Blacklist.Add(item);
        separate = separateFunctionsAndVariables;
        if (seed.HasValue)
        {
            // A shuffled index pool avoids exposing the order in which symbols were found
            // while keeping the result stable for a given seed.
            shuffled = Enumerable.Range(0, 5000).ToList();
            var random = new Random(unchecked((int)(seed.Value ^ (seed.Value >> 32))));
            for (var i = shuffled.Count - 1; i > 0; i--) { var j = random.Next(i + 1); (shuffled[i], shuffled[j]) = (shuffled[j], shuffled[i]); }
        }
    }

    internal void RegisterFunction(string name) => Register(name, separate ? "f" : "o", separate ? functionCounter++ : globalCounter++);
    internal void RegisterVariable(string name) => Register(name, separate ? "v" : "o", separate ? variableCounter++ : globalCounter++);
    private void Register(string name, string prefix, int sequence)
    {
        // GLSL keywords, swizzles, blacklisted symbols, and blank names are never renamed.
        if (string.IsNullOrWhiteSpace(name) || Blacklist.Contains(name.Trim()) || GlslVariables.IsReserved(name)) return;
        if (!Symbols.ContainsKey(name)) Symbols[name] = prefix + (shuffled == null ? sequence : shuffled[sequence % shuffled.Count]).ToString();
    }
}

internal static class GlslVariables
{
    // This set covers the identifiers that the parser can encounter but must not rename.
    private static readonly HashSet<string> Reserved = new(StringComparer.Ordinal)
    {
        "lowp","mediump","highp","precision","attribute","const","uniform","varying","in","out","inout",
        "centroid","flat","smooth","noperspective","invariant","void","bool","int","uint","float","double",
        "vec2","vec3","vec4","bvec2","bvec3","bvec4","ivec2","ivec3","ivec4","uvec2","uvec3","uvec4",
        "dvec2","dvec3","dvec4","mat2","mat3","mat4","mat2x2","mat2x3","mat2x4","mat3x2","mat3x3",
        "mat3x4","mat4x2","mat4x3","mat4x4","sampler1D","sampler2D","sampler3D","samplerCube",
        "sampler1DShadow","sampler2DShadow","samplerCubeShadow","break","continue","do","else","for","if",
        "discard","return","switch","case","default","while","true","false","main","gl_FragColor",
        "gl_FragCoord","gl_Position","gl_VertexID","gl_InstanceID","gl_FragDepth","gl_PointSize","texture2D",
        "textureCube","texture","dot","mix","clamp","fract","sin","cos","tan","asin","acos","atan","pow",
        "exp","log","exp2","log2","sqrt","inversesqrt","abs","sign","floor","ceil","min","max","step",
        "smoothstep","length","distance","cross","normalize","reflect","refract","x","y","z","w","r","g",
        "b","a","xy","xz","yz","rgb","rgba","st","stp","diffuse","ambient","specular","position",
        "spotDirection","spotExponent","spotCutoff","constantAttenuation","linearAttenuation","quadraticAttenuation"
    };
    internal static bool IsReserved(string value) => Reserved.Contains(value);
}

internal static class Engine
{
    // The parser intentionally uses regular expressions rather than a full GLSL grammar:
    // declarations are the only syntax needed to build a safe rename table.
    private static readonly Regex Word = new(@"\b([a-zA-Z_][a-zA-Z0-9_]*)\b", RegexOptions.Compiled);
    private static readonly Regex Keep = new(@"(?://|/\*)\s*@keep\s+([\w\s,]+)", RegexOptions.Compiled);
    private static readonly Regex Io = new(@"\b(?:(?:attribute|const|uniform|varying|in|out|inout|centroid|flat|smooth|noperspective|invariant)|layout\s*\([^)]*\)\s*(?:in|out)?)\s+(?:\w+\s+)*(\w+)\s+([\w\s,\[\]]+);", RegexOptions.Compiled);
    private static readonly Regex Declaration = new(@"\b(?:(?:(?:lowp|mediump|highp|precision|const)\s+)*)(?:void|bool|int|uint|float|double|vec[234]|u?ivec[234]|bvec[234]|dvec[234]|mat[234](?:x[234])?|sampler[123]D(?:Shadow)?|samplerCube(?:Shadow)?)\s+([\w\s,\[\]=().+/*-]+?)(?=[;{])", RegexOptions.Compiled);
    private static readonly Regex Identifier = new(@"\b([a-zA-Z_][a-zA-Z0-9_]*)\b", RegexOptions.Compiled);

    internal static string ClearComments(List<string> lines, bool minify)
    {
        // Join first so block comments spanning several input lines are removed as one comment.
        var raw = Regex.Replace(string.Join("\n", lines), @"/\*[^*]*\*+(?:[^/*][^*]*\*+)*/", " ");
        var result = new StringBuilder();
        foreach (var original in raw.Split('\n'))
        {
            var line = original;
            var comment = line.IndexOf("//", StringComparison.Ordinal);
            if (comment >= 0) line = line.Substring(0, comment);
            if (minify) { line = Regex.Replace(line.Trim(), @"\s+", " "); if (line.Length > 0) result.Append(line).Append('\n'); }
            else result.Append(line.TrimEnd()).Append('\n');
        }
        return result.ToString();
    }

    internal static void CollectSymbols(List<string> lines, ObfuscationContext context)
    {
        foreach (var source in lines)
        {
            var line = source.Trim();
            if (line.Length == 0) continue;
            foreach (Match match in Keep.Matches(line)) foreach (var symbol in Regex.Split(match.Groups[1].Value, @"[,\s]+")) if (symbol.Length > 0) context.Blacklist.Add(symbol.Trim());
            line = Regex.Replace(Regex.Replace(line, @"//.*", ""), @"/\*.*?\*/", "").Trim();
            if (line.Length == 0 || line.StartsWith("#", StringComparison.Ordinal)) continue;
            // Shader inputs and outputs are part of the external interface and must not change.
            foreach (Match match in Io.Matches(line)) foreach (var part in match.Groups[2].Value.Split(',')) { var name = CleanIdentifier(part); if (name.Length > 0) context.Blacklist.Add(name); }
            foreach (Match match in Declaration.Matches(line))
            {
                var declarations = match.Groups[1].Value;
                var function = Regex.Match(declarations, @"^([a-zA-Z_]\w*)\s*\(");
                if (function.Success)
                {
                    context.RegisterFunction(function.Groups[1].Value);
                    var start = declarations.IndexOf('('); var end = declarations.LastIndexOf(')');
                    if (start >= 0 && end > start) foreach (var parameter in SplitTopLevel(declarations.Substring(start + 1, end - start - 1))) { var name = CleanIdentifier(parameter); if (name.Length > 0) context.RegisterVariable(name); }
                }
                else foreach (var part in SplitTopLevel(declarations)) { var declaration = part; var equals = declaration.IndexOf('='); if (equals >= 0) declaration = declaration.Substring(0, equals); var name = CleanIdentifier(declaration); if (name.Length > 0) context.RegisterVariable(name); }
            }
        }
    }

    internal static string ApplyObfuscation(List<string> lines, ObfuscationContext context, bool minify)
    {
        var output = new StringBuilder(); var inDirective = false;
        foreach (var source in lines)
        {
            var trimmed = source.Trim(); if (trimmed.Length == 0) { if (!minify) output.Append('\n'); continue; }
            var directive = trimmed.StartsWith("#", StringComparison.Ordinal);
            // Macro continuations are processed as a directive so their identifiers remain consistent.
            if (inDirective || directive) { inDirective = trimmed.EndsWith("\\", StringComparison.Ordinal); output.Append(Rewrite(minify ? trimmed : source, context)).Append('\n'); }
            else output.Append(Rewrite(minify ? trimmed : source, context)).Append('\n');
        }
        return output.ToString();
    }

    private static string Rewrite(string line, ObfuscationContext context) => Word.Replace(line, match =>
    {
        var position = match.Index; var p = position - 1;
        while (p >= 0 && char.IsWhiteSpace(line[p])) p--;
        // A member after '.' is a built-in/property name, not a local variable.
        var property = p >= 0 && line[p] == '.';
        var word = match.Groups[1].Value;
        return !property && context.Symbols.TryGetValue(word, out var replacement) && !context.Blacklist.Contains(word) ? replacement : word;
    });

    internal static string RemoveNewLines(string code)
    {
        var result = new StringBuilder(); var macro = false;
        foreach (var original in code.Split('\n'))
        {
            var line = original.Trim(); if (line.Length == 0) continue;
            // Preprocessor directives must remain line-oriented; ordinary shader code can be joined.
            if (line.StartsWith("#", StringComparison.Ordinal)) { if (result.Length > 0 && result[result.Length - 1] != '\n') result.Append('\n'); macro = line.EndsWith("\\", StringComparison.Ordinal); if (macro) result.Append(line.Substring(0, line.Length - 1).TrimEnd()).Append(' '); else result.Append(line).Append('\n'); continue; }
            if (macro) { macro = line.EndsWith("\\", StringComparison.Ordinal); if (macro) line = line.Substring(0, line.Length - 1).TrimEnd(); if (line.Length > 0) result.Append(line).Append(macro ? " " : "\n"); else if (!macro) result.Append('\n'); }
            else result.Append(line).Append(' ');
        }
        return result.ToString().Trim();
    }

    private static string CleanIdentifier(string raw) { raw = Regex.Replace(raw, @"\[.*?\]", "").Trim(); var match = Identifier.Match(raw); return match.Success ? match.Groups[1].Value : ""; }
    private static List<string> SplitTopLevel(string input)
    {
        var result = new List<string>(); var current = new StringBuilder(); var depth = 0;
        foreach (var c in input) { if (c == '(' || c == '[' || c == '{') depth++; else if (c == ')' || c == ']' || c == '}') depth = Math.Max(0, depth - 1); if (c == ',' && depth == 0) { if (current.Length > 0) result.Add(current.ToString().Trim()); current.Clear(); } else current.Append(c); }
        if (current.ToString().Trim().Length > 0) result.Add(current.ToString().Trim()); return result;
    }
}
