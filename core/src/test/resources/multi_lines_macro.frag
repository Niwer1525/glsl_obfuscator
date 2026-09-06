#define THIS_IS_A_MULTILINE_MACRO(pos, size) \
{ \
    int i = size.y; \
    \
    int j = pos.x; \
    \
    vec4 color = vec4(i / size.y, j / size.x, 0.0, 1.0); \
}

#define THIS_IS_ANOTHER_MULTILINE_MACRO(relPos, size, Rotation, rotPivot, dSide, uSide, nSide, eSide, sSide, wSide) \
{ \
    vec3 rayDir = normalize(MATRIX * dirTBN); \
    \
    vec3 pivot = rotPivot * OBJECT_SIZE; \
    vec3 pos = relPos * OBJECT_SIZE; \
    \
    color = some_compute_function(ro_loc, rd_loc, size * OBJECT_SIZE, normMat, color, minT, uSide, dSide, nSide, wSide, sSide, eSide); \
}