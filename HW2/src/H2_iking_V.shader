#version 450

layout (location = 0) in vec3 iPosition; // VBO: vbo[0]
uniform mat4 rotMatrix;

out vec4 pos;

void main(void) {
	gl_Position = vec4(iPosition, 1.0) * rotMatrix;
	pos = gl_Position;
}