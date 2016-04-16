layout (location = 0) in vec3 position; 
layout (location = 1) in vec3 normalVec; 
layout (location = 2) in vec2 texCoord; 

out vec2 tex;
out vec3 pos;
out vec3 normal;

layout (std140) uniform Matrices{	
	uniform mat4 projection;
	uniform mat4 view;
};

uniform mat4 model;


void main(){ 
	pos = vec3(model * vec4(position, 1.0f));
	gl_Position = projection * view * model * vec4(position, 1.0); 
	tex = vec2(texCoord.x, texCoord.y);
	normal = mat3(model) * normalVec;  
}