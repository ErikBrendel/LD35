layout (location = 0) in vec3 position; 
layout (location = 1) in vec3 normalVec; 
layout (location = 2) in vec2 texCoord; 

out vec2 tex;

uniform mat4 model;

void main(){ 
	gl_Position = model * vec4(position, 1.0); 
	tex = texCoord;
	gl_Position = vec4(position, 0.1 * model[3][3]); 
}