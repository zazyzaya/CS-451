
#version	450	
uniform float sPos; 
layout (location = 0) in vec3 iPosition;
layout (location = 1) in vec3 iColor;

out vec3 color;

void	main(void)	{	
	//gl_Position = vec4(sPos, sPos, 0.0, 1.0);	
	
	gl_Position = vec4(iPosition.x+sPos, iPosition.y+sPos, 0.0, 1.0);	
	color = iColor;	
	
}