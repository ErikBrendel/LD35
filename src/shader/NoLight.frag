in vec2 tex;
in vec3 pos;
in vec3 normal;

in vec3 debug;

out vec4 color;

struct Material{
	sampler2D texture_diffuse0;
	sampler2D texture_specular0;
};


uniform Material material;

void main(){
	color = texture(material.texture_diffuse0, tex);	
	float gamma = 2.0;
   	color.xyz = pow(color.xyz, vec3(1.0/gamma));
	color.xyz = debug;
}
