out vec4 color;
in vec2 tex;

uniform sampler2D depthMap;

void main()
{             
    float depthValue = texture(depthMap, tex).r;
    color = vec4(vec3(depthValue), 1);
}