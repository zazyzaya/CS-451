#version	450	

out vec4 color; 

void main(void) { 

	if (gl_FragCoord.x	<	200)  //fragment in device coordinates 
		color =	vec4(1.0,	0.0,	0.0,	1.0);	
	else
	if (gl_FragCoord.x	<	400)  //fragment in device coordinates 
		color =	vec4(0.0,	1.0,	0.0,	1.0);	
	else
	if (gl_FragCoord.x	<	600)  //fragment in device coordinates 
		color =	vec4(0.0,	0.0,	1.0,	1.0);	
	else 	
		color =	vec4(1.0,	1.0,	1.0,	1.0);	
		
		
}
