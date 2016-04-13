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
	float constant;
	float linear;
	float quadratic;

	public SpotLight(Vector3f color, Vector3f position, Vector3f direction, float cutOff, float outerCutOff, float constant, float linear, float quadratic) {
		super(color);
		this.position = position;
		this.direction = direction;
		this.cutOff = cutOff;
		this.outerCutOff = outerCutOff;
		this.constant = constant;
		this.linear = linear;
		this.quadratic = quadratic;
	}

	public SpotLight(Vector3f color, Vector3f position, Vector3f direction, float cutOff, float outerCutOff, float reach) {
		super(color);
		this.position = position;
		this.direction = direction;
		this.cutOff = cutOff;
		this.outerCutOff = outerCutOff;
		this.constant = 1.0f;
		this.linear = generateLinearAmount(reach);
		this.quadratic = generateQuadraticAmount(reach);
	}

	/**
	 * generates an approximation of this tables inear amount column:
	 * http://www.learnopengl.com/#!Lighting/Light-casters based on this
	 * function restorer: http://www.arndt-bruenner.de/mathe/scripts/regrnl.htm
	 *
	 * @param distance
	 *            the desired reach distance of the light
	 * @return the linear part for attenuation calculation
	 */
	private static float generateLinearAmount(float distance) {
		return (float) (4.767566446388858 / distance);
	}

	/**
	 * generates an approximation of this tables quadratic amount column:
	 * http://www.learnopengl.com/#!Lighting/Light-casters based on this
	 * function restorer: http://www.arndt-bruenner.de/mathe/scripts/regrnl.htm
	 *
	 * @param distance
	 *            the desired distance for the light
	 * @return the quadratic amount for attenuation calculation
	 */
	private static float generateQuadraticAmount(float distance) {
		return (float) (0.0361492d / distance + 48.572348116d / (distance * distance) + 280d / (distance * distance * distance));
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
		glUniform1f(shader.getUniform(uniform + ".cutoff"), (float) Math.cos(Math.toRadians(cutOff)));
		glUniform1f(shader.getUniform(uniform + ".outerCutoff"), (float) Math.cos(Math.toRadians(outerCutOff)));
		glUniform1f(shader.getUniform(uniform + ".constant"), constant);
		glUniform1f(shader.getUniform(uniform + ".linear"), linear);
		glUniform1f(shader.getUniform(uniform + ".quadratic"), quadratic);
		super.apply(shader, uniform);
	}

	public float[] getData() {
		float[] data = new float[16];

		data[0] = position.x;
		data[1] = position.y;
		data[2] = position.z;

		data[4] = direction.x;
		data[5] = direction.y;
		data[6] = direction.z;

		data[8] = color.x;
		data[9] = color.y;
		data[10] = color.z;

		data[11] = (float) Math.cos(Math.toRadians(cutOff));

		data[12] = (float) Math.cos(Math.toRadians(outerCutOff));

		data[13] = constant;

		data[14] = linear;

		data[15] = quadratic;

		return data;
	}

}
