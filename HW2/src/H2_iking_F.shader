#version 450

in vec4 gl_FragCoord;
in vec4 pos;
out vec4 fColor;

void main(void) {
	float depth = gl_FragCoord.z;
	
	fColor = pos + 0.3;
	fColor = fColor * -log(depth);
}