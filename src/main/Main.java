/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import util.Settings;

/**
 * Main class for LD project
 */
public class Main {
	private static SpaceScene scene;

	public static void main(String[] args) {
		Settings.loadFromFile();
		scene = new SpaceScene();
		scene.start();
	}

}