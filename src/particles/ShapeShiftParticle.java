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
	private static final float startDisplacement = 0.001f;
	

	@Override
	Particle getInstance() {
		return new ShapeShiftParticle();
	}

	@Override
	void generateStartValues(Random ran, Vector3f origin) {
		float dx = (ran.nextFloat() - 0.5f) * startDisplacement;
		float dy = (ran.nextFloat() - 0.5f) * startDisplacement;
		float dz = (ran.nextFloat() - 0.5f) * startDisplacement;
		startPos = new Vector3f(dx, dy, dz);
		
		startDir = new Vector3f((ran.nextFloat() - 0.5f), (ran.nextFloat() - 0.5f), (ran.nextFloat() - 0.5f));
		startDir.normalise();
	}

	@Override
	Matrix4f generateModel(Random ran, float deltaTime) {
		timePassed += deltaTime;
		
		Vector3f trans = new Vector3f(timePassed, timePassed, timePassed);
		
		Matrix4f model = new Matrix4f();
		model.translate(trans);
		model.rotate(timePassed, new Vector3f(1, 0, 0));
		return null;
	}

	@Override
	boolean isDead() {
		return timePassed > lifetime;
	}
	
}