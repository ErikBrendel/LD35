/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

/**
 *
 * @author Erik
 */
public class Balancing {

	public static void init() {
		startTime = System.currentTimeMillis();
	}

	private static int getMS() {
		return (int) (System.currentTimeMillis() - startTime);
	}

	private static long startTime;

	public static float getTimeModelShrinking() {
		return getTimeModelExpanding();
	}

	public static float getTimeModelExpanding() {
		return 0.8f * (float)Math.pow(getGameSpeed(), -0.7f) / 2;
	}

	public static float getPowerupStrength() {
		return 1f;
	}

	public static float getEnemySpeed() {
		return 0.08f * (float)Math.pow(getGameSpeed(), 1.3f);
	}

	/**
	 * should return 1 for "normal speed", 2 means doubled speed
	 *
	 * @param currentMesh the current shape of the player
	 * @return
	 */
	public static float getPlayerSpeed(int currentMesh) {
		float shapeBaseSpeed;
		if(currentMesh == 0) {
			shapeBaseSpeed = 0.14f;
		} else if (currentMesh == 1) {
			shapeBaseSpeed = 0.18f;
		} else {
			shapeBaseSpeed = 0.16f;
		}
		
		return shapeBaseSpeed * getGameSpeed() * 0.7f;
	}

	/**
	 * for internal use only - to generate one "general game speed" to influence
	 * player and enemy equally
	 *
	 * @return a speed multiplier
	 */
	private static float getGameSpeed() {
		return 1f + getMS() / 40000f;
	}
}
