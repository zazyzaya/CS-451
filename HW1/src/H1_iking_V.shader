#version	450	
layout (location = 0) in vec2 iPosition; // VBO: vbo[0]
layout (location = 1) in vec4 iColor;    // VBO: vbo[1]

uniform float theta;
uniform float dust_size;
uniform float mover_size;

out vec4 color; // output to fragment shader

void main(void)	{	
	if (iPosition.x == 0.0 && iPosition.y == 1.0) {
		gl_PointSize = mover_size;
		gl_Position = vec4(cos(theta), sin(theta), 0.0, 1.0);
	}
	else {	
		gl_PointSize = dust_size;
		gl_Position = vec4(iPosition.x, iPosition.y, 0.0, 1.0);	
	}
	
	color = iColor;	
}