/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import util.Animation;
import util.ObjectLoader;

/**
 *
 * @author Erik
 */
public class _TEST_ {
	public static void main(String[] args) {
		Animation test = ObjectLoader.loadAnimation("dummy.dae");
		System.err.println(test);
	}
}
