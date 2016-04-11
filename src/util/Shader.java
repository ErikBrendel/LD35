/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.net.URL;
import java.util.Scanner;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL20.*;

/**
 * Create a pair of shaders (vert+frag), load them and use them
 */
public class Shader {

	private int shaderProgram;

	public static Shader fromFile(String vertexPath, String fragmentPath) {
		return new Shader(getSource(vertexPath), getSource(fragmentPath));
	}

	public Shader(String vertex, String fragment) {

		// loading shaders
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertex);
		glCompileShader(vertexShader);
		int vertCompileSuccess = glGetShader(vertexShader, GL_COMPILE_STATUS);
		System.err.println("Vertex log: " + glGetShaderInfoLog(vertexShader, 512));
		if (vertCompileSuccess != 1) {
			System.err.println("Error compilng vertex shader: " + vertCompileSuccess);
			Display.destroy();
			System.exit(-1);
		}

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragment);
		glCompileShader(fragmentShader);
		int fragCompileSuccess = glGetShader(fragmentShader, GL_COMPILE_STATUS);
		System.err.println("Fragment log: " + glGetShaderInfoLog(fragmentShader, 512));
		if (fragCompileSuccess != 1) {
			System.err.println("Error compiling fragment shader: " + fragCompileSuccess);
			Display.destroy();
			System.exit(-1);
		}

		shaderProgram = glCreateProgram();
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		int progLinkSucccess = glGetProgram(shaderProgram, GL_LINK_STATUS);
		System.err.println("Linking log: " + glGetProgramInfoLog(shaderProgram, 512));
		if (progLinkSucccess != 1) {
			System.err.println("Error linking shader program: " + progLinkSucccess);
			Display.destroy();
			System.exit(-1);
		}
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
	}

	public void use() {
		glUseProgram(shaderProgram);
	}

	public int getUniform(String name) {
		int loc = glGetUniformLocation(shaderProgram, name);
		if (loc < 0) {
			System.err.println("GetUniform failed: " + name);
		} else {
			// System.err.println("uniform set: " + name);
		}
		return loc;
	}

	public static String getSource(String name) {
		try {
			URL shaderURL = Util.class.getResource("/shader/" + name).toURI().toURL();
			Scanner sc = new Scanner(shaderURL.openStream(), "UTF-8");
			String val = sc.useDelimiter("\\A").next();
			sc.close();
			return val;
		} catch (Exception ex) {
			System.err.println("Loading shader source failed:");
			ex.printStackTrace();
			System.exit(0);
			return "";
		}
	}
}
