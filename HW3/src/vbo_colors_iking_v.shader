#version 450

layout (location = 0) in vec4 iPosition; // VBO: vbo[0]
layout (location = 1) in vec4 iColor;
uniform mat4 modelMatrix;

out vec4 color;

void main(void) {
	gl_Position = modelMatrix * iPosition;
	color = iColor;
}