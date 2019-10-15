#version 450

layout (location = 0) in vec4 iPosition; // VBO: vbo[0]
uniform mat4 modelMatrix;


void main(void) {
	gl_Position = modelMatrix * iPosition;
}