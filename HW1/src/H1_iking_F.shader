#version	450	

in  vec4 color; // (interpolated) value from vertex shader
out vec4 fColor; // out to display

void main(void) { 
 	if (color.w != 0.0) {
 		fColor = color;
 	} 
 	
 	else {
	 	if (length(vec2(gl_FragCoord.xy)) >= 1) {
	 		fColor = vec4(1.0, 1.0, 1.0, 1.0);
	 	}
	 	else {
	 		fColor = color;
	 	}
	}
}