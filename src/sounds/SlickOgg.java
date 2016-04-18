/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package sounds;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

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
	
	public static void load() {
		try {
			audio.put("music", AudioLoader.getStreamingAudio("OGG", ResourceLoader.getResource("audio/m_title.ogg").toURI().toURL()));
			isMusic.put("music", false);
		} catch (Exception ex) {
			System.err.println("Cannot load audio data:");
			ex.printStackTrace();
		}
	}

	public static void play(String name) {
		if(isMusic.get(name)) {
			audio.get(name).playAsMusic(1f, 10f, true);
			System.err.println("name = " + name);
		} else {
			audio.get(name).playAsSoundEffect(1, 1, false);
		}
	}
}
