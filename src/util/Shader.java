/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Create a pair of shaders (vert+frag), load them and use them
 */
public class Shader {

	private int shaderProgram;
	private Map<String, Object> parameters;
	private String vertexSource = "";
	private String fragmentSource = "";
	private Map<Integer, String> uniformBlocks;

	public static Shader fromFile(String vertexPath, String fragmentPath) {
		return Shader.fromFile(vertexPath, fragmentPath, new HashMap<>());
	}

	public static Shader fromFile(String vertexPath, String fragmentPath, Map<String, Object> parameters) {
		return new Shader(getSource(vertexPath), getSource(fragmentPath), parameters);
	}

	public Shader(String vertex, String fragment) {
		this(vertex, fragment, new HashMap<>());
	}

	/**
	 * The main constructor of a shader object.
	 *
	 * @param vertexSource
	 *            the content of a Vertex shader
	 * @param fragmentSource
	 *            the content of a fragment shader
	 * @param parameters
	 *            a map containing initial values for shader parameters
	 */
	public Shader(String vertexSource, String fragmentSource, Map<String, Object> parameters) {
		this.parameters = parameters;
		this.vertexSource = vertexSource;
		this.fragmentSource = fragmentSource;
		uniformBlocks = new HashMap<Integer, String>();

		recompile();
	}

	public void addUniformBlockIndex(int index, String name) {
		uniformBlocks.put(index, name);
		setUniformBlockIndex(index, name);
	}

	private void setUniformBlockIndex(int index, String name) {
		int uniformBlockIndex = glGetUniformBlockIndex(shaderProgram, name);
		glUniformBlockBinding(shaderProgram, uniformBlockIndex, index);
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

	public String getParameter(String name) {
		Object obj = parameters.get(name);
		return obj == null ? "" : obj.toString();
	}

	/**
	 * update a shader-parameter
	 *
	 * @param name
	 *            the parameter name, like used in the shader file
	 * @param value
	 *            the value of the parameter
	 * @param update
	 *            wether to directly re-compile the shader after (when false is
	 *            passed, call shader.recompile() to view the results)
	 */
	public void updateParameter(String name, Object value, boolean update) {
		parameters.put(name, value);
		if (update) {
			recompile();
		}
	}

	public void updateParameter(String name, Object value) {
		updateParameter(name, value, true);
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

	/**
	 * re-compiles the shader with the current values of the parameters-Map
	 */
	public void recompile() {
		// generating parameters precompiler actions
		String paramString = "#version 330 core " + System.getProperty("line.separator");
		for (String key : parameters.keySet()) {
			String value = getParameter(key);
			paramString += "#define " + key + " " + value + System.getProperty("line.separator");
		}

		// loading shaders
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, paramString + vertexSource);
		glCompileShader(vertexShader);
		int vertCompileSuccess = glGetShader(vertexShader, GL_COMPILE_STATUS);
		if (vertCompileSuccess != 1) {
			System.err.println("Vertex log: " + glGetShaderInfoLog(vertexShader, 512));
			System.err.println("Error compilng vertex shader: " + vertCompileSuccess);
			Display.destroy();
			System.exit(-1);
		}

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, paramString + fragmentSource);
		glCompileShader(fragmentShader);
		int fragCompileSuccess = glGetShader(fragmentShader, GL_COMPILE_STATUS);
		if (fragCompileSuccess != 1) {
			System.err.println("Fragment log: " + glGetShaderInfoLog(fragmentShader, 512));
			System.err.println("Error compiling fragment shader: " + fragCompileSuccess);
			Display.destroy();
			System.exit(-1);
		}

		shaderProgram = glCreateProgram();
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		int progLinkSucccess = glGetProgram(shaderProgram, GL_LINK_STATUS);
		if (progLinkSucccess != 1) {
			System.err.println("Linking log: " + glGetProgramInfoLog(shaderProgram, 512));
			System.err.println("Error linking shader program: " + progLinkSucccess);
			Display.destroy();
			System.exit(-1);
		}
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		for (int key : uniformBlocks.keySet()) {
			setUniformBlockIndex(key, uniformBlocks.get(key));
		}
	}
}
