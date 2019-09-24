
#version	450	
uniform float sPos; // value from JOGL main program

void	main(void)	{	
	gl_Position = vec4(sPos, sPos, 0.0, 1.0);	
}