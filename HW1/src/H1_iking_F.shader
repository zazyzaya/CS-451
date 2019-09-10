#version	450	

in  vec4 color; // (interpolated) value from vertex shader
in  vec4 gl_FragCoord;

uniform float width;
uniform float height;

out vec4 fColor; // out to display

void main(void) { 
 	if (color.w == 1) {
 		fColor = color;
 	} 
 	
 	else {
 		float dist = length(vec2( 
 			(gl_FragCoord.x - (width/2)), 
 			(gl_FragCoord.y - (height/2))
 		));
 		
	 	if (dist < width/2 ) {
	 		fColor = vec4(1.0, 1.0, 1.0, 1.0);
	 	}
	 	else {
	 		fColor = vec4(color.x, color.y, color.z, 0.0);
	 	}
	}
}