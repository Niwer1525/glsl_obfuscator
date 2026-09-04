#version 150

#test_import <math.glsl>

flat in float u, v;

void main() {
    int model = 0;

    #test_import <model_calculator.glsl>

    if(model > 5) {
        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    } else {
        gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
    }
}