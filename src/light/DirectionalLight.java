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

	public DirectionalLight(Color color, Vector3f direction) {
		super(color);
		this.direction = direction;
	}

	public DirectionalLight(Vector3f color, Vector3f direction) {
		super(color);
		this.direction = direction;
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	@Override
	public void apply(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform + ".direction"), direction.x, direction.y, direction.z);
		super.apply(shader, uniform);
	}

	@Override
	public float[] getData() {
		float[] data = new float[8];
		data[0] = direction.x;
		data[1] = direction.y;
		data[2] = direction.z;

		data[4] = color.x;
		data[5] = color.y;
		data[6] = color.z;

		return data;
	}
}
