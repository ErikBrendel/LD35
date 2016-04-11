package util;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 *
 * @author Erik
 */
public class Material {

    int diffuseTex, specularTex;
    private float shininess;

    public Material(int diffuseTex, int specularTex, float shininess) {
        this.diffuseTex = diffuseTex;
        this.specularTex = specularTex;
        this.shininess = shininess;
    }

    public void setDiffuseTex(int diffuseTex) {
        this.diffuseTex = diffuseTex;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public void setSpecularTex(int specularTex) {
        this.specularTex = specularTex;
    }
    
    
    public void apply(Shader shader, String uniform) {
        glUniform1i(shader.getUniform(uniform + ".diffuse"), diffuseTex);
        glUniform1i(shader.getUniform(uniform + ".specular"), specularTex);
        glUniform1f(shader.getUniform(uniform + ".shininess"), shininess);
    }
    
    public void apply(Shader shader) {
        apply(shader, "material");
    }
}
