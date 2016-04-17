package util;

import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 *
 * @author Erik
 */
public class Material {

	int diffuseTex, specularTex;

	public Material(String diffuseTex, String specularTex) {
		this.diffuseTex = Util.loadTexture(diffuseTex);
		this.specularTex = Util.loadTexture(specularTex);
	}

	public Material(int diffuseTex, int specularTex) {
		this.diffuseTex = diffuseTex;
		this.specularTex = specularTex;
	}

	public void setDiffuseTex(int diffuseTex) {
		this.diffuseTex = diffuseTex;
	}

	public void setSpecularTex(int specularTex) {
		this.specularTex = specularTex;
	}

	public void apply(Shader shader, String uniform) {
		glUniform1i(shader.getUniform(uniform + ".texture_diffuse0"), diffuseTex);
		if (specularTex != 0) {
			glUniform1i(shader.getUniform(uniform + ".texture_specular0"), specularTex);
		}
	}

	public void apply(Shader shader) {
		apply(shader, "material");
	}
}
