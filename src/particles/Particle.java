package particles;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import util.Matrix4f;

public abstract class Particle {

	private Matrix4f model;

	private Vector3f startPos;
	private Vector3f startDir;

	private float timePassed;
	private boolean isDead;

	public Particle() {
		model = new Matrix4f();
	}

	abstract Particle getInstance();

	abstract void generateStartValues(Random ran, Vector3f origin);

	abstract Matrix4f generateModel(Random ran, float deltaTime);

}
