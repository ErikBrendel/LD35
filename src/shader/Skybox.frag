#version 330 core

in vec2 tex;

uniform sampler2D skybox;

out vec4 color;

void main(){
	color = texture(skybox, tex) * 0.9f;
}