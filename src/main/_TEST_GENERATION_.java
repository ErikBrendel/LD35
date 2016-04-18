/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import sounds.SlickOgg;

/**
 *
 * @author Erik
 */
public class _TEST_GENERATION_ {
	public static void main(String[] args) {
		//SlickOgg.playSound("m_title");
		SlickOgg.playSound("e_select");
		new Thread() {
			public void run() {
				try {
					Thread.sleep(320000);
				} catch (Exception e) {
					
				}
			}
		}.start();
	}
}
