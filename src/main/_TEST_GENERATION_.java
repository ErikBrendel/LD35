/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import generating.WorldGenerator;
import java.util.Arrays;

/**
 *
 * @author Erik
 */
public class _TEST_GENERATION_ {
	public static void main(String[] args) {
		WorldGenerator gen = new WorldGenerator();
		gen.generate();
		System.err.println(gen.getData());
	}
}
