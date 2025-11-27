#version 330 core

in vec2 vTexCoord;
out vec4 FragColor;

uniform sampler2D u_tex;

void main() {
    FragColor = texture(u_tex, vTexCoord);
}
