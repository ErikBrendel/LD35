
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
uniform int[KEY_FRAME_COUNT * BONE_COUNT] forThisBone; //boolean to save wether a keyframe applies to a bone
uniform float[KEY_FRAME_COUNT] key_frame_time;

uniform float currentTime;


void main(){ 
	int keyFrameCount = KEY_FRAME_COUNT;
	int boneCount = BONE_COUNT;

	//just that the uniforms are used
	mat4 b = bones[0];
	int f = forThisBone[0];
	float t = key_frame_time[0];

	pos = vec3(model * vec4(position, 1.0f));
	gl_Position = projection * view * model * vec4(position, 1.0); 
	tex = vec2(texCoord.x, texCoord.y);
	normal = mat3(transpose(inverse(model))) * normalVec;  
}