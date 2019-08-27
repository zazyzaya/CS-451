#version 430
uniform float r, g, b;
out vec4 color;
void main(void)
{ color = vec4(r, g, b, 1.0); }