#version 450

in vec4 gl_FragCoord;
in vec4 pos;
out vec4 fColor;

void main(void) {
	fColor = -pos + 0.4;
}