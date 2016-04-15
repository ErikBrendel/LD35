in vec2 tex;
in vec3 pos;
in vec3 normal;

out vec4 color;

struct Material{
	sampler2D texture_diffuse0;
	sampler2D texture_specular0;
};

//size 32
struct DirLight{
	vec3 direction;
	vec3 color;
};
	
//size 48
struct PointLight{
	vec3 position;

	vec3 color;

	float constant;
	float linear;
	float quadratic;
};

//size 80
struct SpotLight{
	vec3 position;
	vec3 direction;
	vec3 color;

	float cutoff;
	float outerCutoff;

	float constant;
	float linear;
	float quadratic;
};

float ambientStrength = 0.01f;
float specularStrength = 0.5f;
float reflectionStrength = 0.4f;

layout (std140) uniform Lights{		
	DirLight[
	#if NUM_DIR_LIGHTS == 0
	1
	#else
	NUM_DIR_LIGHTS
	#endif
	] dirLights;
	
	SpotLight[
	#if NUM_SPOT_LIGHTS == 0
	1
	#else
	NUM_SPOT_LIGHTS
	#endif
	] spotLights;
	
	PointLight[
	#if NUM_POINT_LIGHTS == 0
	1
	#else
	NUM_POINT_LIGHTS
	#endif
	] pointLights;
};

uniform Material material;

uniform int alpha;

uniform vec3 viewPos;

uniform samplerCube skybox;

vec3 calcDirectionalLight(DirLight light, vec3 norm, vec3 viewDir);
vec3 calcPointLight(PointLight pl, vec3 norm, vec3 viewDir);
vec3 calcSpotLight(SpotLight light, vec3 norm, vec3 viewDir);

void main(){ 
	vec3 norm = normalize(normal);		

	vec3 viewDir = normalize(viewPos - pos);

	if(dot(norm, viewDir) < 0){
		//norm = -norm;
	}
	
	vec3 result = vec3(0); 
	
	for(int i = 0; i < NUM_POINT_LIGHTS; i++){
		result += calcPointLight(pointLights[i], norm, viewDir);
	}
	
	for(int i = 0; i < NUM_DIR_LIGHTS; i++){
		result += calcDirectionalLight(dirLights[i], norm, viewDir);
	}
	
	for(int i = 0; i < NUM_SPOT_LIGHTS; i++){
		result += calcSpotLight(spotLights[i], norm, viewDir);
	}
	
	vec3 ambient = ambientStrength * vec3(texture(material.texture_diffuse0, tex));

	result += ambient;

	float gamma = 2.0;
   	result = pow(result, vec3(1.0/gamma));
    
	float a = texture(material.texture_specular0, tex).r * reflectionStrength;
	if(a > 0){
		vec3 I = normalize(pos - viewPos);
		vec3 R = reflect(I, normalize(norm));
		vec3 reflectionColor = vec3(texture(skybox, R));
	
		result = mix(result, reflectionColor, a);	
	}
	
	if(alpha == 0){
		color = vec4(result.x, result.y, result.z, 1.0f);
	}else if(alpha == 1){
		vec4 texColor = vec4(result.x, result.y, result.z, (texture(material.texture_diffuse0, tex)).a);
		if(texColor.a > 0.01){	
			color = texColor;
		}else{	
			discard;
		}
	}
}

vec3 calcDirectionalLight(DirLight light, vec3 norm, vec3 viewDir){	
	vec3 direction = normalize(-light.direction);

	float diff = max(dot(norm, direction), 0.0);
	vec3 diffuse = diff * light.color;

	vec3 halfwayDir = normalize(direction + viewDir);
	float spec = pow(max(dot(normal, halfwayDir), 0.0f), SHININESS);
	vec3 specular = specularStrength * spec * light.color;

	return vec3(texture(material.texture_diffuse0, tex)) * diffuse + vec3(texture(material.texture_specular0, tex)) * specular;
}

vec3 calcPointLight(PointLight light, vec3 norm, vec3 viewDir){	
	vec3 direction = light.position - pos;

	float distance = length(direction);
	direction = normalize(direction);

	float diff = max(dot(norm, direction), 0.0);
	vec3 diffuse = diff * light.color;

	vec3 halfwayDir = normalize(direction + viewDir);
	float spec = pow(max(dot(normal, halfwayDir), 0.0f), SHININESS);
	vec3 specular = specularStrength * spec * light.color;
	
	float attenuation = 1.0f / (light.constant + light.linear * distance + light.quadratic * distance * distance);

	diffuse  *= attenuation;
	specular *= attenuation;  

	return vec3(texture(material.texture_diffuse0, tex)) * diffuse + vec3(texture(material.texture_specular0, tex)) * specular;
}

vec3 calcSpotLight(SpotLight light, vec3 norm, vec3 viewDir){

	vec3 direction = light.position - pos;

	float distance = length(direction);
	direction = normalize(direction);	

	float diff = max(dot(norm, direction), 0.0);
	vec3 diffuse = diff * light.color;

	vec3 halfwayDir = normalize(direction + viewDir);
	float spec = pow(max(dot(normal, halfwayDir), 0.0f), SHININESS);
	vec3 specular = specularStrength * spec * light.color;
	
	float attenuation = 1.0f / (light.constant + light.linear * distance + light.quadratic * distance * distance);
	
	float intensity = clamp((dot(direction, normalize(-light.direction)) - light.outerCutoff) / (light.cutoff - light.outerCutoff), 0.0f, 1.0f);

	diffuse *= intensity;
	specular *= intensity * 2;

	diffuse  *= attenuation;
	specular *= attenuation;  

	return vec3(texture(material.texture_diffuse0, tex)) * (diffuse) + vec3(texture(material.texture_specular0, tex)) * specular;
}