/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package light;

import java.awt.Color;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import org.lwjgl.util.vector.Vector3f;
import util.Shader;

/**
 *
 * @author Erik
 */
public class PointLight extends Light {

	Vector3f position;
	float constant;
	float linear;
	float quadratic;

	public PointLight(Vector3f color, Vector3f position, float constant, float linear, float quadratic) {
		super(color);
		this.position = position;
		this.constant = constant;
		this.linear = linear;
		this.quadratic = quadratic;
	}

	public PointLight(Color color, Vector3f position, float reach) {
		super(color);
		this.position = position;
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

	public void setConstant(float constant) {
		this.constant = constant;
	}

	public void setLinear(float lienar) {
		this.linear = lienar;
	}

	public void setQuadratic(float quadratic) {
		this.quadratic = quadratic;
	}

	public Vector3f getPosition() {
		return position;
	}

	@Override
	public void apply(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform + ".position"), position.x, position.y, position.z);
		glUniform1f(shader.getUniform(uniform + ".constant"), constant);
		glUniform1f(shader.getUniform(uniform + ".linear"), linear);
		glUniform1f(shader.getUniform(uniform + ".quadratic"), quadratic);
		super.apply(shader, uniform);
	}

}
