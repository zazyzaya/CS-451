#version 450

layout (location = 0) in vec3 iPosition; // VBO: vbo[0]
uniform mat4 rotMatrix;
uniform int drawLines;

out vec4 pos;

void main(void) {
	gl_Position = vec4(iPosition, 1.0) * rotMatrix;
	
	// Circle gets a special draw mode 
	if (drawLines > 1)
		pos = vec4(0.2, 0.2, 0.2, 1.0);
	
	// Drawing wireframes as lighter colors
	else if (drawLines > 0)
		pos = vec4(vec4(iPosition, 1.0) / 2);
	
	// Normal draw mode
	else 
		pos = vec4(iPosition, 1.0);
}