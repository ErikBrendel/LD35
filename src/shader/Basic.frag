in vec2 tex;
in vec3 pos;
in vec3 normal;

out vec4 color;

struct Material{
	sampler2D texture_diffuse0;
	sampler2D texture_specular0;
};

struct DirLight{
	vec3 direction;
	vec3 color;
};
	
struct PointLight{
	vec3 position;

	vec3 color;

	float constant;
	float linear;
	float quadratic;
};

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

float ambientStrength = 0.1f;
float specularStrength = 0.5f;
float reflectionStrength = 0.4f;

uniform PointLight pointLight;

uniform DirLight dirLight;

uniform SpotLight spotLight;

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
		norm = -norm;
	}
	
	vec3 result = calcPointLight(pointLight, norm, viewDir);
	
	//result += calcDirectionalLight(dirLight, norm, viewDir);

	result += calcSpotLight(spotLight, norm, viewDir);
	
	vec3 ambient = ambientStrength * vec3(texture(material.texture_diffuse0, tex));

	result += ambient;

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
	vec3 dir = normalize(-light.direction);

	float diff = max(dot(norm, dir), 0.0);
	vec3 diffuse = diff * light.color;

	vec3 reflectDir = reflect(-dir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0f), SHININESS);
	vec3 specular = specularStrength * spec * light.color;

	return vec3(texture(material.texture_diffuse0, tex)) * diffuse + vec3(texture(material.texture_specular0, tex)) * specular;
}

vec3 calcPointLight(PointLight light, vec3 norm, vec3 viewDir){	
	vec3 direction = light.position - pos;

	float distance = length(direction);
	direction = normalize(direction);

	float diff = max(dot(norm, direction), 0.0);
	vec3 diffuse = diff * light.color;

	vec3 reflectDir = reflect(-direction, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0f), SHININESS);
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

	vec3 reflectDir = reflect(-direction, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0f), SHININESS);
	vec3 specular = specularStrength * spec * light.color;
	
	float attenuation = 1.0f / (light.constant + light.linear * distance + light.quadratic * distance * distance);
	
	float intensity = clamp((dot(direction, normalize(-light.direction)) - light.outerCutoff) / (light.cutoff - light.outerCutoff), 0.0f, 1.0f);

	diffuse *= intensity;
	specular *= intensity * 2;

	diffuse  *= attenuation;
	specular *= attenuation;  

	return vec3(texture(material.texture_diffuse0, tex)) * (diffuse) + vec3(texture(material.texture_specular0, tex)) * specular;
}