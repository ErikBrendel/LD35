package particles;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import util.Matrix4f;

public abstract class Particle {

	protected Matrix4f model;

	protected Vector3f startPos;
	protected Vector3f startDir;

	protected float timePassed;
	protected boolean dead;

	public Particle() {
		model = new Matrix4f();
	}

	abstract Particle getInstance();

	abstract void generateStartValues(Random ran, Vector3f origin);

	abstract Matrix4f generateModel(Random ran, float deltaTime);
	
	abstract boolean isDead();

}
