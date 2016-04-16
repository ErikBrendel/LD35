
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normalVec;
layout (location = 2) in vec2 texCoord;
layout (location = 3) in float[BONE_COUNT] boneWeight;

out vec2 tex;
out vec3 pos;
out vec3 normal;
out vec3 debug;

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

	vec4 posAnim;
	vec4 normAnim;

	//calculate bone state for each bone at current frame
	for (int b = 0; b < BONE_COUNT; b++) {
		
		//get the keyframes we need to interpolate between
		int beforeKeyFrame = 0;
		int afterKeyFrame = 0;
		for (int k = 0; k < KEY_FRAME_COUNT; k++) {
			if (currentTime < key_frame_time[k]) {
				//this is the after keyframe!
				beforeKeyFrame = k - 1;
				afterKeyFrame = k;
				break;
			}
		}

		float beforeT = key_frame_time[beforeKeyFrame];
		float afterT = key_frame_time[afterKeyFrame];
		float interpolFactor = (currentTime - beforeT) / (afterT - beforeT);

		if (afterKeyFrame == 0 && beforeKeyFrame == 0) { //extrapolation to right
			//before all 
			afterKeyFrame = KEY_FRAME_COUNT - 1;
			beforeKeyFrame = KEY_FRAME_COUNT - 1;
			interpolFactor = 0;
		}/**/
		if (beforeKeyFrame < 0) { //extrapolation to the left
			beforeKeyFrame = 0;
			interpolFactor = 0;
		}

		mat4 beforeM = bones[beforeKeyFrame * BONE_COUNT + b];
		mat4 afterM = bones[afterKeyFrame * BONE_COUNT + b];

		

		mat4 interpolated = (beforeM * (1.0 - interpolFactor)) + (afterM * interpolFactor);
		//interpolated = inverse(interpolated);

		//debug = vec3(interpolated[0][1],interpolated[1][1],interpolated[2][1]);
		//debug = vec3(boneWeight[0] * boneWeight[0] * boneWeight[0], boneWeight[1], b / float(BONE_COUNT));
		float weight = boneWeight[1 - b];
		if (weight < 0) {
			weight = -weight;
		}
		debug = vec3(weight);
		
		posAnim = posAnim + ((interpolated * vec4(position, 1)) * boneWeight[b]);
		normAnim = normAnim + ((interpolated * vec4(normalVec, 1)) * boneWeight[b]);
		//posAnim.w = 1.0;
		//normAnim.w = 1.0;

	}

	//posAnim = posAnim + vec4(position, 0);
	//normAnim = normAnim + vec4(normalVec, 0);

	posAnim.w = 1.0;
	normAnim.w = 1.0;
	

	pos = vec3(model * posAnim);
	gl_Position = projection * view * model * posAnim; 
	tex = vec2(texCoord.x, texCoord.y);
	normal = vec3(model * normAnim);
}