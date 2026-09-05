// Variable defined in shader importing this one.
// Function defined in math.glsl imported into the shader importing this one.
model_calculator = random(42);

// This variable is defined in the shader importing this one and will be kept since it is annotated with @keep in the shader importing this one.
color = vec4(uv, 0.0, 1.0);