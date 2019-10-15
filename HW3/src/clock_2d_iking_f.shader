#version 450

uniform vec4 color;
out vec4 fColor;

void main(void) {
	fColor = color;
}