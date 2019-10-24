#version 450

layout (location = 0) in vec4 iPosition; // VBO: vbo[0]
layout (location = 1) in vec4 iColor;
layout (location = 2) in vec4 iNormal;

uniform mat4 modelview_mx;
uniform mat4 projection_mx;

out vec4 color;

void main(void) { 
	// Lighting stuff here
	color = iColor;
	
	gl_Position = projection_mx * modelview_mx * iPosition;
}