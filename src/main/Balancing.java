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
		return (int)(System.currentTimeMillis() - startTime);
	}
	
	private static long startTime;
	
	public static float getTimeModelShrinking() {
		return 0.4f;
	}
	
	public static float getTimeModelExpanding() {
		return 0.4f;
	}

	public static float getPowerupStrength() {
		return 2f;
	}

	public static float getEnemySpeed() {
		return 0.08f + (float) Math.sqrt(getMS() / 1000f) / 90;
	}

	/**
	 * should return 1 for "normal speed", 2 means doubled speed
	 * @param currentMesh the current shape of the player
	 * @return 
	 */
	public static float getPlayerSpeed(int currentMesh) {
		return 1f;
	}
}
