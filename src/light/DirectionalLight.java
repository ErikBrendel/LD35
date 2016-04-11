package light;

import java.awt.Color;
import static org.lwjgl.opengl.GL20.glUniform3f;
import org.lwjgl.util.vector.Vector3f;
import util.Shader;

/**
 *
 * @author Erik
 */
public class DirectionalLight extends Light {
    
    private Vector3f direction;

    public DirectionalLight(Color color, float intensity, Vector3f direction) {
        super(color, intensity);
        this.direction = direction;
    }
    
    public DirectionalLight(Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f direction) {
        super(ambient, diffuse, specular);
        this.direction = direction;
    }

    public void setPosition(Vector3f position) {
        this.direction = position;
    }

    @Override
    public void apply(Shader shader, String uniform) {
        glUniform3f(shader.getUniform(uniform + ".direction"), direction.x, direction.y, direction.z);
        super.apply(shader, uniform);
    }
    
    
    
}
