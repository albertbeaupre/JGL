
#version 330 core

in vec2 absolutePosition;
in vec2 localPosition;
in vec2 halfSize;
in float shapeType;
in vec4 fillColor;
in float borderThickness;
in vec4 borderColor;
in vec4 additionalVariables;

out vec4 fragColor;

float rectangle(in vec2 p, in vec2 b, in vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

float circle(vec2 p, float r) {
    return length(p) - r;
}

float triangle(vec2 p, vec2 a, vec2 b, vec2 c) {
    vec2 e0 = b - a;
    vec2 e1 = c - b;
    vec2 e2 = a - c;

    vec2 v0 = p - a;
    vec2 v1 = p - b;
    vec2 v2 = p - c;

    float d0 = dot(e0 * clamp(dot(v0, e0) / dot(e0, e0), 0.0, 1.0) - v0,
    e0 * clamp(dot(v0, e0) / dot(e0, e0), 0.0, 1.0) - v0);

    float d1 = dot(e1 * clamp(dot(v1, e1) / dot(e1, e1), 0.0, 1.0) - v1,
    e1 * clamp(dot(v1, e1) / dot(e1, e1), 0.0, 1.0) - v1);

    float d2 = dot(e2 * clamp(dot(v2, e2) / dot(e2, e2), 0.0, 1.0) - v2,
    e2 * clamp(dot(v2, e2) / dot(e2, e2), 0.0, 1.0) - v2);

    float s = sign(e0.x * v0.y - e0.y * v0.x);
    s += sign(e1.x * v1.y - e1.y * v1.x);
    s += sign(e2.x * v2.y - e2.y * v2.x);

    float dist = sqrt(min(d0, min(d1, d2)));

    return (s < 2.0 ? dist : -dist);
}


void main() {
    float aa = 0.001; // Small AA value to minimize outline
    float distance = 0;
    vec4 color = vec4(0.0);

    if (shapeType == 0) {
        vec2 b = additionalVariables.xy;
        float lineThickness = additionalVariables.z;
        float outerThickness = lineThickness + borderThickness;

        vec2 pa = localPosition;
        vec2 ba = b;

        float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
        vec2 closest = ba * h;
        float d = length(pa - closest);

        float fill = smoothstep(lineThickness + aa, lineThickness - aa, d);
        float stroke = smoothstep(outerThickness + aa, outerThickness - aa, d) * (1.0 - fill);

        color = fillColor * fill + borderColor * stroke;
        if (color.a < 0.01) discard;
        fragColor = color;
        return;
    }

    if (shapeType == 1) {
        distance = rectangle(localPosition, halfSize, additionalVariables);
    } else if (shapeType == 2) {
        float radius = additionalVariables.x;
        distance = circle(localPosition, radius);
    } else if (shapeType == 3) {
        vec2 v1 = additionalVariables.xy;
        vec2 v2 = additionalVariables.zw;
        vec2 v0 = -(v1 + v2);
        distance = triangle(localPosition, v0, v1, v2);
    }

    if (borderThickness <= 0.0) {
        color = fillColor * smoothstep(aa, -aa, distance);
    } else {
        float fill = smoothstep(aa, -aa, distance + borderThickness);
        float stroke = smoothstep(aa, -aa, distance) - smoothstep(aa, -aa, distance + borderThickness);
        color = fillColor * fill + borderColor * stroke;
    }

    if (color.a < 0.01) discard;
    fragColor = color;
}
