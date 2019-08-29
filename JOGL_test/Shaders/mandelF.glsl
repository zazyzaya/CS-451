#version 430
out vec4 color;
void main(void)
{
	vec2 c, z;
	float i;
	z = vec2(gl_Position.x, gl_Position.y);
	c = vec2(gl_Position.x, gl_Position.y);

	while(i = 0; i<25; i++) {
		float x = (z.x * z.x - z.y * z.y) + c.x;
        float y = (z.y * z.x + z.x * z.y) + c.y;

        if((x * x + y * y) > 4.0) break;
        z.x = x;
        z.y = y;
	}

	color = vec4(0.0, i*1.0, i*1.0, 1.0);
}