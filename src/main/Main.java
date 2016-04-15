/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

/**
 * Main class for LD project
 */
public class Main {
	private static SpaceScene scene;

	public static void main(String[] args) {
		scene = new SpaceScene();
		scene.start();
	}

}