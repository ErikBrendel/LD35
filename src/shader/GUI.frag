in vec2 tex;

out vec4 color;

struct Material{
	sampler2D texture_diffuse0;
	sampler2D texture_specular0;
};

uniform Material material;

void main(){
	vec4 a = texture(material.texture_specular0, tex);
	color = texture(material.texture_diffuse0, tex);
	
}
