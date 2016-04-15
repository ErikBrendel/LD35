out vec4 color;
in vec2 tex;

uniform sampler2D depthMap;

uniform float near; 
uniform float far; 

float LinearizeDepth(float depth) 
{
    float z = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - z * (far - near));	
}

void main()
{             
    float depthValue = LinearizeDepth(gl_FragCoord.z);
    color = vec4(vec3(depthValue), 1.0f);
}  