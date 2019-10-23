#version 450

uniform mat4 modelview_mx;
uniform mat4 perspective_mx;

in  vec4 color; // (interpolated) value from vertex shader
out vec4 iColor;

void main(void) {
	iColor = color;
}