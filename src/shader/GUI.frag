in vec2 tex;

out vec4 color;

sampler2D icon;


void main(){
	color = texture(icon, tex);
	if(color.a == 0){
		discard;
	}
}
