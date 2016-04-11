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

	private Vector3f color;

	public Light(Vector3f color) {
		this.color = color;
	}

	public Light(Color color) {
		this.color = new Vector3f(color.getRed(), color.getGreen(), color.getBlue());
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}

	public void apply(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform + ".color"), color.x / 255.0f, color.y / 255.0f, color.z / 255.0f);
	}
}
