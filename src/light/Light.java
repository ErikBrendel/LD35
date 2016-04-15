package light;

import java.awt.Color;

import static org.lwjgl.opengl.GL20.glUniform3f;

import org.lwjgl.util.vector.Vector3f;

import util.Scene;
import util.Shader;

/**
 *
 * @author Erik
 */
public abstract class Light {

	protected Vector3f color;
	private static int count = 0;
	private int id;

	protected static Shader shadowShader;

	static {
		shadowShader = Shader.fromFile("Shadow.vert", "Shadow.frag");
	}

	public Light(Vector3f color) {
		this.color = color;
		this.id = count;
		count++;
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Light) {
			return id == ((Light) obj).id;
		}
		return false;
	}

	public abstract float[] getData();

	public abstract void renderShadows(Scene scene);
}
