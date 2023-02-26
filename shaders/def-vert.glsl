#version 400 core

uniform float time;
uniform mat4 mvpMat;
uniform mat4 mMat;
out vec3 fCol;
out vec2 fuv;
out vec3 fnormal;
out vec3 spos;
out vec3 wpos;

in vec3 pos;
in vec3 color;
in vec2 uv;
in vec3 normal;

void main(){
	vec4 ppos;
	ppos.rgb=pos.rgb;
	ppos.w=1;
	
	
	gl_Position=mvpMat*ppos;
	//gl_Position = vec4(pos, 1.0) + vec4(sin(time) * 0.5, cos(time) * 0.5, 0.0, 0.0);
	fCol=color;
	fuv=uv;
	fnormal=(mMat*vec4(normal.rgb,0)).rgb;
	fnormal=normalize(fnormal);
	spos=gl_Position.xyz;

	wpos=(mMat*ppos).xyz;

}