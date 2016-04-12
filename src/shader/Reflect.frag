in vec3 norm;
in vec3 pos;
out vec4 color;

uniform vec3 viewPos;
uniform samplerCube skybox;


void main(){
	vec3 I = normalize(pos - viewPos);
	vec3 R = reflect(I, normalize(norm));
	color = texture(skybox, R);
}