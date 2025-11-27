#version 330 core

layout(location = 0) in vec2 vertexPosition;
layout(location = 1) in vec2 instanceLocation; // top-left anchor or bottom-left if inverted y axis
layout(location = 2) in vec2 instanceHalfSize;
layout(location = 3) in vec2 instanceScale;
layout(location = 4) in float instanceRotation;
layout(location = 5) in vec2 instanceRotationOrigin; // rotation pivot from the center (local space)
layout(location = 6) in vec2 instanceOffset; // transform (world space)
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

void main() {
    shapeType = instanceShapeType;
    fillColor = instanceFillColor;
    borderThickness = instanceBorderThickness;
    borderColor = instanceBorderColor;
    additionalVariables = instanceAdditionalVariables;

    vec2 world = vec2(0.0);
    float c = cos(instanceRotation);
    float s = sin(instanceRotation);

    if (shapeType == 0.0) {
        // Line: A = instanceLocation
        vec2 a = instanceLocation;
        vec2 ab = instanceAdditionalVariables.xy;
        float thickness = instanceAdditionalVariables.z;
        float cap = thickness + borderThickness;

        vec2 dir = normalize(ab);
        float len = length(ab);
        vec2 n = vec2(-dir.y, dir.x);

        float t = (vertexPosition.x * 0.5 + 0.5);
        float along = mix(-cap, len + cap, t);
        float off = vertexPosition.y * cap;

        localPosition = dir * along + n * off;// local: A = (0,0)
    } else {
        halfSize = instanceHalfSize * instanceScale;
        localPosition = vertexPosition * halfSize;
    }

    // Rotation relative to pivot in local space
    vec2 origin = instanceRotationOrigin;
    vec2 p = localPosition - origin;
    vec2 rotated = vec2(p.x * c - p.y * s, p.x * s + p.y * c) + origin;

    // World placement by shape type
    if (shapeType == 3.0) { // Triangle: anchor = centroid
        world = instanceLocation + rotated + instanceOffset;
    } else if (shapeType == 0.0) { // Line: anchor = start point
        world = instanceLocation + rotated + instanceOffset;
    } else if (shapeType == 2.0) { // Circle: anchor = center
        world = instanceLocation + rotated + instanceOffset;
    } else {
        world = instanceLocation + rotated + halfSize + instanceOffset;
    }

    absolutePosition = instanceLocation;
    vec4 finalPos;
    if (u_useProjection) {
        finalPos = u_projection * vec4(world, 0.0, 1.0);
    } else {
        vec2 ndc = (world / u_resolution) * 2.0 - 1.0;
        ndc.y = -ndc.y;
        finalPos = vec4(ndc, 0.0, 1.0);
    }

    gl_Position = finalPos;
}
