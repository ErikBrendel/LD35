layout (location = 0) in vec3 position; 
layout (location = 1) in vec3 normalVec; 
layout (location = 2) in vec2 texCoord;
layout (location = 3) in mat4 instanceMatrix;

out vec2 tex;
out vec3 pos;
out vec3 normal;

layout (std140) uniform Matrices{	
	uniform mat4 projection;
	uniform mat4 view;
};



void main(){ 
	pos = vec3(instanceMatrix * vec4(position, 1.0f));
	gl_Position = projection * view * instanceMatrix * vec4(position, 1.0); 
	tex = vec2(texCoord.x, texCoord.y);
	normal = mat3(instanceMatrix) * normalVec;  
}