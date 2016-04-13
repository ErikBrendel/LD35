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

	protected Vector3f color;

	public Light(Vector3f color) {
		this.color = color;
	}

	public Light(Color color) {
		this.color = new Vector3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}

	public void apply(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform + ".color"), color.x, color.y, color.z);
	}
}
