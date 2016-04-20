struct Material{
	sampler2D texture_diffuse0;
	sampler2D texture_specular0;
};

uniform Material material;

void main()
{             	
	if(texture(material.texture_diffuse0, vec2(0, 0)).r == 0.1 || texture(material.texture_specular0, vec2(0, 0)).r == 0.1){
		gl_FragDepth = gl_FragCoord.z;
	}
}  