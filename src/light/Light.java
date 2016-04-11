package light;

import java.awt.Color;
import static org.lwjgl.opengl.GL20.glUniform3f;
import org.lwjgl.util.vector.Vector3f;
import util.Shader;

/**
 *
 * @author Erik
 */
public class Light {

    private Vector3f ambient, diffuse, specular;

    public Light(Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }
    
    public Light(Color color, float intensity) {
        this.ambient = new Vector3f(0.02f, 0.02f, 0.02f);
        this.specular = new Vector3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        this.diffuse = new Vector3f(color.getRed() / 255f * intensity, color.getGreen() / 255f * intensity, color.getBlue() / 255f * intensity);
    }

    public void setAmbient(Vector3f ambient) {
        this.ambient = ambient;
    }

    public void setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
    }

    public void setSpecular(Vector3f specular) {
        this.specular = specular;
    }

    public void apply(Shader shader, String uniform) {
        glUniform3f(shader.getUniform(uniform + ".ambient"), ambient.x, ambient.y, ambient.z);
        glUniform3f(shader.getUniform(uniform + ".diffuse"), diffuse.x, diffuse.y, diffuse.z);
        glUniform3f(shader.getUniform(uniform + ".specular"), specular.x, specular.y, specular.z);
    }
}
