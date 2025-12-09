#version 330 core

layout(location = 0) in vec2 vertexPosition;          // -1..1 quad
layout(location = 1) in vec2 instanceLocation;
layout(location = 2) in vec2 instanceHalfSize;
layout(location = 3) in vec2 instanceScale;
layout(location = 4) in float instanceRotation;
layout(location = 5) in vec2 instanceRotationOrigin;
layout(location = 6) in vec2 instanceOffset;
layout(location = 7) in float instanceShapeType;
layout(location = 8) in vec4 instanceFillColor;
layout(location = 9) in float instanceBorderThickness;
layout(location = 10) in vec4 instanceBorderColor;
layout(location = 11) in vec4 instanceAdditionalVariables;

uniform bool u_useProjection;
uniform vec2 u_resolution;
uniform mat4 u_projection;

out vec2 absolutePosition;
out vec2 localPosition;
out vec2 halfSize;
out float shapeType;
out vec4 fillColor;
out float borderThickness;
out vec4 borderColor;
out vec4 additionalVariables;

mat2 rotation(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

void main() {
    // Convert quad -1..1 into local space (scaled to half size)
    vec2 local = vertexPosition * instanceHalfSize;

    // Apply scale
    local *= instanceScale;

    // Move pivot
    local -= instanceRotationOrigin;

    // Rotate
    local = rotation(instanceRotation) * local;

    // Move pivot back
    local += instanceRotationOrigin;

    // Final world position
    vec2 world = instanceLocation + instanceOffset + local;

    // Outputs
    localPosition = local;
    absolutePosition = world;
    halfSize = instanceHalfSize;
    shapeType = instanceShapeType;
    fillColor = instanceFillColor;
    borderThickness = instanceBorderThickness;
    borderColor = instanceBorderColor;
    additionalVariables = instanceAdditionalVariables;

    if (u_useProjection) {
        gl_Position = u_projection * vec4(world, 0.0, 1.0);
    } else {
        // convert directly from pixel coords
        vec2 ndc = (world / u_resolution) * 2.0 - 1.0;
        ndc.y = -ndc.y; // flip, because OpenGL Y is up
        gl_Position = vec4(ndc, 0.0, 1.0);
    }
}
