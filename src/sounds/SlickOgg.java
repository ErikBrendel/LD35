/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package sounds;

import java.util.HashMap;
import java.util.Set;
import org.lwjgl.openal.AL;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;
import util.Settings;

/**
 *
 * @author Erik
 */
public class SlickOgg {

	private static HashMap<String, Audio> audio;
	private static HashMap<String, Boolean> isMusic;

	static {
		audio = new HashMap<>();
		isMusic = new HashMap<>();
	}

	public static void loadSound(String name) {
		try {
			audio.put(name, AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("audio/" + name + ".ogg")));
			isMusic.put(name, name.startsWith("m"));
		} catch (Exception ex) {
			System.err.println("Error loading Sound " + name + ".ogg:");
			ex.printStackTrace();
		}
	}

	public static void playSound(String name) {
		if (!audio.containsKey(name)) {
			loadSound(name);
		}

		if (isMusic.get(name)) {
			if (Settings.getBoolean("music_enabled")) {
				audio.get(name).playAsSoundEffect(1, 1, true);
			}
		} else {
			if (Settings.getBoolean("sounds_enabled")) {
				audio.get(name).playAsSoundEffect(1, 1, false);
			}
		}
	}

	public static void destroy() {
		AL.destroy();
	}
}
