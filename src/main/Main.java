/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import light.PointLight;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.Player;
import util.Shader;
import util.Util;

/**
 * Main class for LD project
 */
public class Main {

	static float deltaTime = 0;
	static long lastFrame = 0;
	static Player player;

	public static void main(String[] args) {
		System.out.println("test!");

		// create window
		Point windowSize;
		try {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			windowSize = new Point((int) screen.getWidth(), (int) screen.getHeight());
			DisplayMode full = Util.getBestDisplayMode();
			Display.setDisplayMode(full);
			Display.setFullscreen(true);
			Display.setVSyncEnabled(true);
			Display.setTitle("Learning openGL with Java");
			Display.create();

			Mouse.create();
			Mouse.setGrabbed(true);
			glViewport(0, 0, windowSize.x, windowSize.y);
		} catch (Exception e) {
			System.out.println("Error setting up display");
			System.exit(-1);
		}

		Shader defaultShader = Shader.fromFile("default.vert", "default.frag");
		defaultShader.use();
		glEnable(GL_DEPTH_TEST);

		Util.loadTexture("minecraft.png", 0);
		Material mat = new Material(0, 0, 32);
		mat.apply(defaultShader);

		// light
		PointLight pl = new PointLight(Color.yellow, 1, new Vector3f(2, 2, 2), 50);
		pl.apply(defaultShader, "pointLights[0]");

		// bunny
		Mesh bunny = new Mesh("bunny.obj");
		int bunnyVAO = bunny.generateVAO();

		player = new Player();

		// game loop
		while (!Display.isCloseRequested()) {

			long currentFrame = System.nanoTime();
			deltaTime = (float) ((currentFrame - lastFrame) / 1000000d / 1000d);
			System.out.println("FPS = " + (double) 1 / deltaTime);
			lastFrame = currentFrame;

			defaultShader.use();
			handleInputs(deltaTime, defaultShader);
			player.update(deltaTime);

			// render
			glClearColor(0.05f, 0.075f, 0.075f, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			Matrix4f view = player.getViewMatrix();
			glUniformMatrix4(defaultShader.getUniform("view"), false, view.getData());

			Matrix4f projection = player.getProjectionMatrix();
			glUniformMatrix4(defaultShader.getUniform("projection"), false, projection.getData());

			Matrix4f model = new Matrix4f();
			model.translate(new Vector3f(0, 0, -1));
			glUniformMatrix4(defaultShader.getUniform("model"), false, model.getData());

			glBindVertexArray(bunnyVAO);
			glDrawArrays(GL_TRIANGLES, 0, bunny.getVertCount());
			glBindVertexArray(0);
			// finish frame
			Display.update();
			Display.sync(500);
		}
	}

	private static void handleInputs(float deltaTime, Shader defaultShader) {
		Mouse.poll();

		// zooming
		int scrollDelta = Mouse.getDWheel();
		// System.err.println("scrollDelta = " + scrollDelta);
		player.getCamera().processMouseScroll(scrollDelta * deltaTime);
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			player.getCamera().processMouseScroll(-60 * deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			player.getCamera().processMouseScroll(60 * deltaTime);
		}

		// moving
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			Display.destroy();
			System.exit(0);
		}
	}
}
