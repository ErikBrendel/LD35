#define NR_POINT_LIGHTS 4  

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
};

struct DirLight {
    vec3 direction;
    
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
struct PointLight {    
    vec3 position;
    
    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
struct SpotLight {
    vec3 position;
    vec3 direction;

    float cutOff;
    float outerCutOff;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

out vec4 fragColor;

uniform Material material;
uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLight;
uniform int spotLightEnabled;

uniform vec3 viewPos;
uniform vec3 viewDir;

in vec2 texCoord;
in vec3 fragCoord;
in vec3 normal;

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec3 texColor, vec3 specColor);
vec3 calcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 texColor, vec3 specColor);
vec3 calcSpotlight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir);

void main() {
    vec3 _pos = viewDir;
    SpotLight _s = spotLight;

    //get normalized view and normal vectors
    vec3 view = normalize(viewPos - fragCoord);
    vec3 norm = normalize(normal);
    
    //get texture color values
    vec3 texColor = vec3(texture(material.diffuse, texCoord));
    vec3 specColor = vec3(texture(material.specular, texCoord));

    vec3 color = vec3(0, 0, 0);
    color += calcDirLight(dirLight, norm, view, texColor, specColor);

    for (int i = 0; i < NR_POINT_LIGHTS; i++) {
        PointLight pl = pointLights[i];
        color += calcPointLight(pl, norm, fragCoord, view, texColor, specColor);    
    }

    if(spotLightEnabled == 1) {
        color += calcSpotlight(spotLight, norm, fragCoord, view);
    }

    fragColor = vec4(color, 1.0);/**/
}


vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec3 texColor, vec3 specColor) {
    vec3 lightDir = normalize(-light.direction);
    //calc diffuse light intensity
    float diff = max(dot(normal, lightDir), 0.0);
    //calc specular light intensity
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    //combine results
    vec3 ambient = light.ambient * texColor;
    vec3 diffuse = light.diffuse * diff * texColor;
    vec3 specular = light.specular * spec * specColor;
    return (ambient + diffuse + specular);
}

vec3 calcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 texColor, vec3 specColor) {
    vec3 lightDir = normalize(light.position - fragPos);
    //diffuse light component
    float diff = max(dot(normal, lightDir), 0.0);
    //specular light component
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    //Attenuation (making light less effective over distance)
    float dist = length(light.position - fragPos);
    float attDenomVal = (light.constant + light.linear * dist + 
                                light.quadratic * dist * dist);
    if (attDenomVal == 0.0) { //endless bright light? perhaps no data set. return no light impact

        return vec3(0, 0, 0);

    }
    float attenuation = 1.0f / attDenomVal;
    //combine results
    vec3 ambient = light.ambient * attenuation * texColor;
    vec3 diffuse = light.diffuse * diff * attenuation * texColor;
    vec3 specular = light.specular * spec * attenuation * specColor;
    return (ambient + diffuse + specular);
}

vec3 calcSpotlight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir = normalize(light.position - fragPos);
    
    //get angle theta (between that ray and the spotlights general direction), also as cosinr val
    float theta = dot(lightDir, normalize(-light.direction));

    //check if we are in reach
    if (theta < light.outerCutOff) {
        return vec3(0, 0, 0);
    }

    //smooth cone borders
    float intensity = (theta - light.outerCutOff) / (light.cutOff - light.outerCutOff);
    intensity = clamp(intensity, 0.0f, 1.0f);
    
    //diffuse light component
    float diff = max(dot(normal, lightDir), 0.0);
    //specular light component
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);

    //Attenuation (making light less effective over distance)
    float dist = length(light.position - fragPos);
    float attDenomVal = (1.0 + 0.045 * dist + 
                                0.0075 * dist * dist);
    float attenuation = 1.0f / attDenomVal;
    
    //get texture color values
    vec3 texColor = vec3(texture(material.diffuse, texCoord));
    vec3 specColor = vec3(texture(material.specular, texCoord));
    //combine results
    vec3 ambient = light.ambient * texColor;
    vec3 diffuse = light.diffuse * diff * texColor;
    vec3 specular = light.specular * spec * specColor;
    ambient *= intensity;
    diffuse *= intensity;
    specular *= intensity;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    return (ambient + diffuse + specular);
}
