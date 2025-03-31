import re
import tkinter as tk
from tkinter import filedialog

# List of GLSL keywords to avoid minifying
GLSL_KEYWORDS = {
    "void", "int", "float", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4",
    "uniform", "attribute", "varying", "const", "return", "if", "else", "for", "while",
    "break", "continue", "discard", "sampler2D", "samplerCube",
    "gl_FragColor", "gl_FragCoord", "gl_Position", "gl_VertexID",
    "texture2D", "textureCube", "dot", "mix", "clamp", "fract", "sin", "cos",
    "switch", "main"
}

GLSL_SWIZZLES = {"x", "y", "z", "w", "r", "g", "b", "a", "xy", "xz", "yz", "rgb", "rgba", "st", "stp"}

def minify_shader_code(code):
    # Minify the shader code by removing comments and extra spaces
    minified_code = minify_glsl(code)
    
    # Minify variable names
    minified_code = minify_variable_names(minified_code)

    return minified_code

def minify_glsl(code):
    lines = code.splitlines()
    minified_lines = []

    for line in lines:
        if line.strip().startswith("#"):  
            minified_lines.append(line.strip() + "\n") # Keep preprocessor directives
        else:
            # Delete tabs
            line = line.replace("\t", "")

            # Deletion of multi-line comments
            line = re.sub(r'/\*.*?\*/', '', line, flags=re.S)

            # Deletion of single-line comments
            line = re.sub(r'//.*', '', line)

            # Deleting extra spaces and tabs
            line = re.sub(r'([,;{}()=+\-*/])\s+', r'\1', line) # Deleting spaces after the symbols
            line = re.sub(r'\s+([,;{}()=+\-*/])', r'\1', line) # Deleting spaces before the symbols

            if line: # Do not add empty lines
                minified_lines.append(line)

    return "".join(minified_lines)

def minify_variable_names(code):
    name_map = {}
    name_counter = 0
    uniforms = set()
    variables = []

    # Split the code into preprocessor and shader lines
    lines = code.splitlines()
    preprocessor_lines = []
    shader_lines = []
    
    for line in lines:
        if line.strip().startswith("#"):  
            preprocessor_lines.append(line.strip()) # Keep preprocessor directives
        else:
            shader_lines.append(line)

    shader_code = "\n".join(shader_lines)

    # Find uniforms (e.g: uniform vec4 sceneColor;)
    for match in re.finditer(r'uniform\s+\w+\s+(\w+);', shader_code):
        uniforms.add(match.group(1))

    # Find declared variables (e.g: float a; vec3 b; mat4 c;)
    for match in re.finditer(r'\b(?:float|vec[234]|mat[234])\s+(\w+)', shader_code):
        var_name = match.group(1)
        if var_name not in GLSL_KEYWORDS and var_name not in uniforms:
            variables.append(var_name)

    # Generate a mapping for variable names to minified names
    for var in variables:
        if var not in name_map:
            name_map[var] = f"v{name_counter}"  # E.G: v0, v1, v2...
            name_counter += 1

    def replace_name(match):
        original_name = match.group(0)
        # Check if the name is a keyword or a uniform
        if original_name in GLSL_KEYWORDS or original_name in uniforms or original_name in GLSL_SWIZZLES:
            return original_name
        return name_map.get(original_name, original_name)

    # We use a regex to replace variable names in the shader code
    pattern = r'(?<!\.)\b[a-zA-Z_][a-zA-Z0-9_]*\b'
    minified_shader_code = re.sub(pattern, replace_name, shader_code)

    # Rebuild the preprocessor lines with the minified shader code
    return "\n".join(preprocessor_lines) + "\n" + minified_shader_code

# Test #
root = tk.Tk()
root.withdraw()

shader_code = ""
shader_file = filedialog.askopenfilename()
with open(shader_file, 'r') as file:
    shader_code = file.read()

print(minify_shader_code(shader_code))
