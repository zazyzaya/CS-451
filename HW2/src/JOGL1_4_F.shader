#version	450	

in  vec3 color; // (interpolated) value from vertex shader
out vec4 fColor; // out to display


void main(void) { 

	 //fColor = vec4(1.0, 0.0, 0.0, 0.1); 
 	fColor = vec4(color, 0.1); 
		
		
}
