#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 _normal;
layout (location = 2) in vec2 _texCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 texCoord;
out vec3 fragCoord;
out vec3 normal;

void main() {
    //only apply model matrix first
    vec4 modelSpace = model * vec4(position, 1.0);

    //apply the rest for final coordinates
    gl_Position = projection * view * modelSpace;

    //pass world space coords to fragment shader
    fragCoord = modelSpace.xyz;

    //apply generated normal matrix to normal data
    normal = mat3(transpose(inverse(model))) * _normal;

    texCoord = _texCoord;
}