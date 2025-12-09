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

float drawRect(vec2 p) {
    vec2 d = abs(p) - halfSize;
    float inside = max(d.x, d.y);
    return inside;
}

float drawCircle(vec2 p) {
    float r = halfSize.x;
    return length(p) - r;
}

float drawTriangle(vec2 p) {
    // upright triangle
    float w = halfSize.x;
    float h = halfSize.y;

    vec2 a = vec2(0, h);
    vec2 b = vec2(-w, -h);
    vec2 c = vec2(w, -h);

    // signed-distance function
    float d1 = (p.x - a.x)*(b.y - a.y) - (p.y - a.y)*(b.x - a.x);
    float d2 = (p.x - b.x)*(c.y - b.y) - (p.y - b.y)*(c.x - b.x);
    float d3 = (p.x - c.x)*(a.y - c.y) - (p.y - c.y)*(a.x - c.x);

    bool inside = (d1 <= 0.0 && d2 <= 0.0 && d3 <= 0.0);

    // not an exact sdf for triangle; simple "inside only"
    return inside ? -1.0 : 1.0;
}

float drawLine(vec2 p) {
    // Line direction is halfSize
    vec2 dir = halfSize;
    float len = length(dir);
    vec2 n = dir / len;

    float t = dot(p, n);
    t = clamp(t, -len, len);

    vec2 closest = n * t;
    return length(p - closest) - borderThickness;
}

void main() {
    float sdf;

    if (shapeType == 0.0)       sdf = drawRect(localPosition);
    else if (shapeType == 1.0)  sdf = drawCircle(localPosition);
    else if (shapeType == 2.0)  sdf = drawTriangle(localPosition);
    else if (shapeType == 3.0)  sdf = drawLine(localPosition);
    else sdf = 1.0;

    // Fill + border
    float dist = sdf;

    // Border region: [-borderThickness, 0]
    float borderMask = smoothstep(0.0, -borderThickness, dist);

    // Fill mask: dist < 0
    float fillMask = dist < 0.0 ? 1.0 : 0.0;

    vec4 col = mix(borderColor, fillColor, fillMask);
    col.a *= (fillMask > 0.0 || borderMask > 0.0) ? 1.0 : 0.0;

    if (col.a <= 0.0)
    discard;

    fragColor = col;
}
