#version 400 core

uniform sampler2D tex;
uniform vec3 lPos;
uniform vec3 lDiff;
uniform vec3 lAten;
uniform vec3 viewPos;


in vec3 fCol;
in vec2 fuv;
in vec3 fnormal;
in vec3 spos;
in vec3 wpos;

layout(location = 0) out vec4 color;
				
void main()
{

	color=texture2D(tex,fuv);
	color.rgb+=fCol.rgb*.16;
	color.a*=.5;
	
	

	
	vec3 lightDir = wpos - lPos;  
	float di=length(lightDir);

	float d=max(dot(fnormal,normalize(lightDir)),0);
	vec3 diffuse = d * lDiff;
	
	vec3 viewDir = -normalize(viewPos - wpos);
	vec3 reflect = reflect(-normalize(lightDir), fnormal);  
	
	float spec = pow(max(dot(viewDir, reflect), 0.0), 32);
	float specular = 3.55 * spec;
	
	float str=1/(lAten.x*lAten.x*di+lAten.y*di+lAten.z);

	color.rgb*=(diffuse+specular)*str+.1;

	//color.rgb*=(dot(normalize(vec3(0,-1,5)),fnormal))*2.2;

}
