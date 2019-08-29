#version 430
uniform float offsetX, offsetY, rot;

mat4 buildRotateZ(float rad)
{ 
	mat4 zrot = mat4(
		cos(rad), -sin(rad), 0.0, 0.0,
		sin(rad), cos(rad), 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		0.0, 0.0, 0.0, 1.0 
	);
	return zrot;
}

void main(void)
{ 
	if (gl_VertexID == 0) gl_Position = vec4(0.25+offsetX, -0.25+offsetY, 0.0, 1.0);
	else if (gl_VertexID == 1) gl_Position = vec4(-0.25+offsetX, -0.25+offsetY, 0.0, 1.0);
	else if (gl_VertexID == 2) gl_Position = vec4(0.25+offsetX, 0.25+offsetY, 0.0, 1.0);
	else gl_Position = vec4(-0.25+offsetX, 0.25+offsetY, 0.0, 1.0);
	
	mat4 zrot = buildRotateZ(rot);
	gl_Position = zrot * gl_Position;
}