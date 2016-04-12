#version 330 core 

layout (location = 0) in vec3 position; 
layout (location = 1) in vec3 normalVec; 
layout (location = 2) in vec2 texCoord; 

out vec2 tex;
out vec3 pos;
out vec3 normal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main(){ 
	pos = vec3(model * vec4(position, 1.0f));
	gl_Position = projection * view * model * vec4(position, 1.0); 
	tex = vec2(texCoord.x, 1.0f - texCoord.y);
	normal = mat3(transpose(inverse(model))) * normalVec;  
}