#version 330 core

layout(location = 0) in vec2 aPos;// base quad [0,1]x[0,1]
layout(location = 1) in vec2 iPos;// instance position
layout(location = 2) in vec2 iSize;// width, height
layout(location = 3) in vec2 iOrigin;// origin (pivot) in pixels
layout(location = 4) in float iRot;// degrees
layout(location = 5) in vec4 iUV;// u0, v0, u1, v1

uniform mat4 u_proj;

out vec2 vTexCoord;

void main() {
    // scale quad to size, shift by origin
    vec2 local = aPos * iSize - iOrigin;

    float rad = radians(-iRot);
    float c = cos(rad);
    float s = sin(rad);

    vec2 rotated = vec2(
    local.x * c - local.y * s,
    local.x * s + local.y * c
    );

    vec2 worldPos = rotated + iOrigin + iPos;

    gl_Position = u_proj * vec4(worldPos, 0.0, 1.0);

    vec2 uv0 = iUV.xy;
    vec2 uv1 = iUV.zw;
    vTexCoord = mix(uv0, uv1, aPos);// aPos in [0,1] drives UV interpolation
}
