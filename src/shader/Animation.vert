#define BONE_COUNT 10
#define KEY_FRAME_COUNT 3

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normalVec;
layout (location = 2) in vec2 texCoord;
layout (location = 3) in float[BONE_COUNT] boneWeight;

out vec2 tex;
out vec3 pos;
out vec3 normal;

layout (std140) uniform Matrices{	
	uniform mat4 projection;
	uniform mat4 view;
};

uniform mat4 model;

uniform mat4[KEY_FRAME_COUNT * BONE_COUNT] bones;
uniform int[Key_FRAME_COUNT * BONE_COUNT] forThisBone; //boolean to save wether a keyframe applies to a bone
uniform float[KEY_FRAME_COUNT] key_frame_time;


void main(){ 
	pos = vec3(model * vec4(position, 1.0f));
	gl_Position = projection * view * model * vec4(position, 1.0); 
	tex = vec2(texCoord.x, texCoord.y);
	normal = mat3(transpose(inverse(model))) * normalVec;  
}