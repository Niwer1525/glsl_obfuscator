using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;

namespace Niwer.GlslObfuscator;

/// <summary>Minifies and obfuscates GLSL using the shared Rust engine.</summary>
public static class GlslObfuscator
{
    /// <summary>Obfuscates shader source supplied as one string.</summary>
    public static string Obfuscate(string content, bool minify = true, bool separateFunctionsAndVariables = false, long? seed = null, IEnumerable<string>? excludedSymbols = null)
    {
        if (content is null) throw new ArgumentNullException(nameof(content));
        return ObfuscateLines(content.Length == 0 ? Array.Empty<string>() : content.Split(new[] { "\n" }, StringSplitOptions.None), minify, excludedSymbols, separateFunctionsAndVariables, seed);
    }
    /// <summary>Reads and obfuscates a shader file without modifying it.</summary>
    public static string ObfuscateFile(string filePath, bool minify = true, bool separateFunctionsAndVariables = false, long? seed = null, IEnumerable<string>? excludedSymbols = null)
    {
        if (filePath is null) throw new ArgumentNullException(nameof(filePath));
        if (!File.Exists(filePath)) throw new FileNotFoundException("GLSL file does not exist.", filePath);
        return ObfuscateLines(File.ReadAllLines(filePath), minify, excludedSymbols, separateFunctionsAndVariables, seed);
    }
    /// <summary>Obfuscates multiple files using one shared symbol table.</summary>
    public static IDictionary<string, string> ObfuscateProject(IEnumerable<string> filePaths, bool minify = true, bool separateFunctionsAndVariables = false, long? seed = null, IEnumerable<string>? excludedSymbols = null)
    {
        if (filePaths is null) throw new ArgumentNullException(nameof(filePaths));
        var files = filePaths.ToList();
        if (files.Count == 0) return new Dictionary<string, string>();
        using var context = new RustContext(excludedSymbols, separateFunctionsAndVariables, seed);
        var sources = new List<(string Path, string Source)>();
        foreach (var path in files.OrderBy(Path.GetFileName, StringComparer.Ordinal))
        {
            if (!File.Exists(path)) throw new FileNotFoundException("GLSL file does not exist.", path);
            var source = File.ReadAllText(path); context.Collect(source); sources.Add((path, source));
        }
        return sources.ToDictionary(item => item.Path, item => context.Transform(item.Source, minify));
    }
    /// <summary>Obfuscates source represented as lines.</summary>
    public static string ObfuscateLines(IEnumerable<string> lines, bool minify = true, IEnumerable<string>? excludedSymbols = null, bool separateFunctionsAndVariables = false, long? seed = null)
    {
        if (lines is null) throw new ArgumentNullException(nameof(lines));
        var source = lines.ToList(); if (source.Count == 0) return string.Empty;
        using var context = new RustContext(excludedSymbols, separateFunctionsAndVariables, seed);
        var text = string.Join("\n", source); context.Collect(text); return context.Transform(text, minify);
    }
}

internal sealed class RustContext : IDisposable
{
    private IntPtr handle;
    internal RustContext(IEnumerable<string>? excluded, bool separate, long? seed)
    {
        using var value = new Utf8Buffer(string.Join("\n", excluded ?? Enumerable.Empty<string>()));
        handle = Native.Create(value.Pointer, separate, seed ?? -1);
        if (handle == IntPtr.Zero) throw new InvalidOperationException("The Rust obfuscator context could not be created.");
    }
    internal void Collect(string source)
    {
        using var value = new Utf8Buffer(source);
        if (!Native.Collect(handle, value.Pointer)) throw new InvalidOperationException("The Rust obfuscator could not collect symbols.");
    }
    internal string Transform(string source, bool minify)
    {
        using var value = new Utf8Buffer(source); var result = Native.Transform(handle, value.Pointer, minify);
        if (result == IntPtr.Zero) throw new InvalidOperationException("The Rust obfuscator could not transform source.");
        try { var length = 0; while (Marshal.ReadByte(result, length) != 0) length++; var bytes = new byte[length]; Marshal.Copy(result, bytes, 0, length); return Encoding.UTF8.GetString(bytes); }
        finally { Native.FreeString(result); }
    }
    public void Dispose() { if (handle != IntPtr.Zero) { Native.FreeContext(handle); handle = IntPtr.Zero; } GC.SuppressFinalize(this); }
    ~RustContext() => Dispose();
    private sealed class Utf8Buffer : IDisposable
    {
        internal readonly IntPtr Pointer;
        internal Utf8Buffer(string value) { var bytes = Encoding.UTF8.GetBytes(value + "\0"); Pointer = Marshal.AllocHGlobal(bytes.Length); Marshal.Copy(bytes, 0, Pointer, bytes.Length); }
        public void Dispose() => Marshal.FreeHGlobal(Pointer);
    }
    private static class Native
    {
        [DllImport("glsl_obfuscator_core", EntryPoint = "glsl_context_collect", CallingConvention = CallingConvention.Cdecl)] [return: MarshalAs(UnmanagedType.I1)] internal static extern bool Collect(IntPtr context, IntPtr source);
        [DllImport("glsl_obfuscator_core", EntryPoint = "glsl_context_new", CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr Create(IntPtr excludedCsv, [MarshalAs(UnmanagedType.I1)] bool separate, long seed);
        [DllImport("glsl_obfuscator_core", EntryPoint = "glsl_context_transform", CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr Transform(IntPtr context, IntPtr source, [MarshalAs(UnmanagedType.I1)] bool minify);
        [DllImport("glsl_obfuscator_core", EntryPoint = "glsl_context_free", CallingConvention = CallingConvention.Cdecl)] internal static extern void FreeContext(IntPtr context);
        [DllImport("glsl_obfuscator_core", EntryPoint = "glsl_string_free", CallingConvention = CallingConvention.Cdecl)] internal static extern void FreeString(IntPtr value);
    }
}
