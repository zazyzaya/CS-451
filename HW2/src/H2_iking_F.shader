#version 450
in  vec4 color; // (interpolated) value from vertex shader
out vec4 fColor;

void main(void) {
	fColor = color;	
}