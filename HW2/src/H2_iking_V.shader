#version 450

layout (location = 0) in vec3 iPosition; // VBO: vbo[0]
layout (location = 1) in vec3 iColor;    // VBO: vbo[1]

uniform mat4 rotMatrix;

out vec4 color;

void main(void) {
	gl_Position = vec4(iPosition, 1.0) * rotMatrix;
	color = vec4(iColor.x, iColor.y, iColor.z, 1.0);
}