/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package particles;

import java.util.Random;
import org.lwjgl.util.vector.Vector3f;
import util.Matrix4f;

/**
 *
 * @author Erik
 */
public class ShapeShiftParticle extends Particle {

	private static final float lifetime = 0.5f;
	private static final float startDisplacement = 0.1f;
	private static Random random = new Random();

	private Vector3f rotationAxis;
	private float lifespan;

	public ShapeShiftParticle() {
	}

	public ShapeShiftParticle(Vector3f axis, float lifespan) {
		axis.normalise();
		this.rotationAxis = axis;
		this.lifespan = lifespan;
	}

	@Override
	Particle getInstance() {
		return new ShapeShiftParticle(new Vector3f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f), 0.5f + random.nextFloat());
	}

	@Override
	void generateStartValues(Random ran, Vector3f origin) {
		float dx = (ran.nextFloat() - 0.5f) * startDisplacement;
		float dy = (ran.nextFloat() - 0.5f) * startDisplacement;
		float dz = (ran.nextFloat() - 0.5f) * startDisplacement;
		startPos = new Vector3f(dx + origin.x, dy + origin.y, dz + origin.z);

		startDir = new Vector3f(ran.nextFloat() - 0.5f, ran.nextFloat() - 0.5f, ran.nextFloat() - 0.5f);
		startDir.normalise();
	}

	@Override
	Matrix4f generateModel(Random ran, float deltaTime) {
		timePassed += deltaTime;

		Vector3f position = Vector3f.add(new Vector3f(timePassed * startDir.x * 0.5f, timePassed * startDir.y * 0.5f, timePassed * startDir.z * 0.5f), startPos, null);

		Matrix4f model = new Matrix4f();
		model.translate(position);
		/*
		 * model.rotate(timePassed * rotationSpeed.x, new Vector3f(1, 0, 0));
		 * model.rotate(timePassed * rotationSpeed.y, new Vector3f(0, 1, 0));
		 * model.rotate(timePassed * rotationSpeed.z, new Vector3f(0, 0, 1));/*
		 */
		model.rotate(timePassed * 3 + 2, rotationAxis);
		model.scale(new Vector3f(lifetime - timePassed, lifetime - timePassed, lifetime - timePassed));
		return model;
	}

	@Override
	boolean isDead() {
		return timePassed > lifetime * lifespan;
	}

}
