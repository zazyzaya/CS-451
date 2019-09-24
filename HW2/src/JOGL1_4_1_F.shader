#version	450	

in  vec3 color; // (interpolated) value from vertex shader
out vec4 oColor; // out to display


void main(void) { 

	 //oColor = vec4(1.0, 0.0, 0.0, 0.1); 
 	oColor = vec4(color, 0.1); 
		
		
}
