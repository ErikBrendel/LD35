#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texPos;

out vec2 tex;

uniform mat4 view;
uniform mat4 projection;

void main(){
	gl_Position = projection * view * vec4(position, 1.0f);
	tex = texPos;
}