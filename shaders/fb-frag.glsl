#version 400 core

uniform sampler2D tex;
uniform vec2 invRes;
uniform float time;
out vec4 colour;
in vec3 fCol;
in vec2 fuv;
in vec3 fnormal;
in vec3 spos;

				
void main()
{

	vec2 screen=gl_FragCoord.xy*invRes;

	screen.x+=sin(time*3+screen.y*50.0+.0515)*.003;
	screen.y+=sin(time*3+screen.x*50.0+.2245)*.003;
	colour=pow(texture2D(tex,screen),vec4(2.2));

}
