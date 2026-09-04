int random(int seed) {
    return int(fract(sin(float(seed)) * 43758.5453) * 100000.0);
}