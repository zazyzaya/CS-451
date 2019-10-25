#version 450

layout (location = 0) in vec4 iPosition; // VBO: vbo[0]
layout (location = 1) in vec4 iColor;
layout (location = 2) in vec4 iNormal;

uniform mat4 modelview_mx;
uniform mat4 cameraview_mx;
uniform mat4 projection_mx;
uniform vec4 light_pos;
uniform int isShaded;

out vec4 color;

void main(void) { 
	gl_Position = projection_mx * modelview_mx * iPosition;
	color = iColor;

	if (isShaded == 0) {
		return;
	}

	// Diffuse material is just whatever's in the VBO for color & light is always white	
	vec4 Ld = vec4(1.0,1.0,1.0,1.0);
	vec4 Md = iColor;
	vec4 Ms = vec4(1.0,1.0,1.0,1.0);
	
	// Ambient 
	vec4 Ia = Md * 0.1;
	
	vec4 pt_position = cameraview_mx * modelview_mx * iPosition;
	vec3 norm_trans = normalize(transpose(inverse(mat3(modelview_mx))) * iNormal.xyz);
	vec3 light_dir = normalize(light_pos.xyz - pt_position.xyz);
	
	// Diffuse
	float nL = max(dot(norm_trans, light_dir), 0);
	vec4 Id = Md * nL;
	
	// Specular
	vec4 Is = Ms * pow( max(dot(norm_trans, light_dir), 0)/length(light_dir), 20 ); 
	
	color = Ia + Id + Is;
	color.w = 1;
}