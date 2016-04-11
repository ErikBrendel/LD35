/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package light;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import org.lwjgl.util.vector.Vector3f;
import util.Shader;

/**
 *
 * @author Erik
 */
public class SpotLight extends Light {

	Vector3f position, direction;
	float cutOff, outerCutOff;

	public SpotLight(Vector3f color, Vector3f position, Vector3f direction, float cutOff, float outerCutOff) {
		super(color);
		this.position = position;
		this.direction = direction;
		this.cutOff = cutOff;
		this.outerCutOff = outerCutOff;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	@Override
	public void apply(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform + ".position"), position.x, position.y, position.z);
		glUniform3f(shader.getUniform(uniform + ".direction"), direction.x, direction.y, direction.z);
		glUniform1f(shader.getUniform(uniform + ".cutOff"), (float) Math.cos(Math.toRadians(cutOff)));
		glUniform1f(shader.getUniform(uniform + ".outerCutOff"), (float) Math.cos(Math.toRadians(outerCutOff)));
		super.apply(shader, uniform);
	}

}
