/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Erik
 */
public class Settings {

	private static final String defaultFile = ""
			+ "# Settings file for \"Worktitle\", a \n"
			+ "# Ludum Dare Game by\n"
			+ "# Markus Brand and Erik Brendel\n"
			+ "skybox_mipmap = true\n"
			+ "light_shader_optimization = true\n"
			+ "sounds_enabled = true\n"
			+ "music_enabled = true";

	private static HashMap<String, String> settings;

	public static void loadFromFile() {
		settings = new HashMap<>();
		List<String> content = loadFileData();
		
		for (String s: content) {
			if(!s.startsWith("#")) {
				String[] lineData = s.split("=");
				settings.put(lineData[0].trim(), (lineData.length > 1) ? lineData[1].trim() : "");
			}
		}
		
	}

	private static List<String> loadFileData() {
		try {
			URL path = Settings.class.getProtectionDomain().getCodeSource().getLocation();
			File me = new File(path.toURI());
			if (me.isFile()) {
				me = me.getParentFile();
			}

			me = new File(me.getAbsolutePath() + "/settings.txt");

			if (me.exists()) {
				//return read data
				return Files.readAllLines(me.toPath());
			} else {
				//write default 
				PrintWriter wr;
				wr = new PrintWriter(me);
				wr.print(defaultFile);
				wr.close();
				return Arrays.asList(defaultFile.split("\n"));
			}
		} catch (Exception ex) {
			return Arrays.asList(defaultFile.split("\n"));
		}
	}

	public static String get(String key) {
		return settings.get(key);
	}

	public static boolean getBoolean(String key) {
		String s = get(key);
		if(s == null) {
			return false;
		}
		return Boolean.valueOf(s);
	}
}
