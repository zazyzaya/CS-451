#version	450	
layout (location = 0) in vec2 iPosition; // VBO: vbo[0]
layout (location = 1) in vec4 iColor;    // VBO: vbo[1]

out vec4 color; // output to fragment shader

void main(void)	{		
	gl_Position = vec4(iPosition.x, iPosition.y, 0.0, 1.0);	
	color = iColor;	
}