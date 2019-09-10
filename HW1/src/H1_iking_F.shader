#version	450	

in  vec4 color; // (interpolated) value from vertex shader
in  vec4 gl_FragCoord;

uniform float width;
uniform float height;

out vec4 fColor; // out to display

vec2 normalize_coords(vec2 coords) {
	float x = coords.x/width;
 	float y = coords.y/height;
 	x = x*2.0 - 1.0;
 	y = y*2.0 - 1.0;
 	
 	return vec2(x,y);
}

void main(void) { 
 	if (color.w == 1) {
 		fColor = color;
 	} 
 	
 	else {
 		vec2 norm_coords = normalize_coords(gl_FragCoord.xy);
 		float dist = length(norm_coords);
 		
	 	if (dist >= 1 && dist <=1.001) {
	 		fColor = vec4(1.0, 1.0, 1.0, 1.0);
	 	}
	 	else {
	 		fColor = vec4(color.x, color.y, color.z, 0.0);
	 	}
	}
}