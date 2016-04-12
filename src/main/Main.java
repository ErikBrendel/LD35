/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static util.ObjectLoader.loadObjectEBO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.HashMap;

import light.PointLight;
import light.SpotLight;

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
import util.Skybox;
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

			glEnable(GL_DEPTH_TEST);
			glEnable(GL_STENCIL_TEST);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

			glViewport(0, 0, windowSize.x, windowSize.y);
		} catch (Exception e) {
			System.out.println("Error setting up display");
			System.exit(-1);
		}

		Skybox skybox = new Skybox("day");

		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("SHININESS", 64);
		Shader defaultShader = Shader.fromFile("Basic.vert", "Basic.frag", parameters);

		defaultShader.use();
		glUniform1i(defaultShader.getUniform("alpha"), 1);

		player = new Player();

		Matrix4f projection = player.getProjectionMatrix();
		glUniformMatrix4(defaultShader.getUniform("projection"), false, projection.getData());

		int dif = Util.loadTexture("container2.png");
		int spec = Util.loadTexture("container2_specular.png");
		// Util.loadTexture("minecraft.png", 2, false);
		Material mat = new Material(dif, spec);

		mat.apply(defaultShader);

		// light
		PointLight pl = new PointLight(Color.WHITE, new Vector3f(2, 2, 2), 10);
		pl.apply(defaultShader, "pointLight");

		SpotLight sl = new SpotLight(new Vector3f(1.0f, 1.0f, 1.0f), player.getCamera().getPosition(), player.getCamera().getDirection(), 10, 20, 40);
		sl.apply(defaultShader, "spotLight");

		// bunny
		// Mesh bunny = new Mesh("bunny.obj");
		Mesh bunny = loadObjectEBO("bunny.obj");

		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		// glLineWidth(100);
		// game loop
		while (!Display.isCloseRequested()) {

			long currentFrame = System.nanoTime();
			deltaTime = (float) ((currentFrame - lastFrame) / 1000000d / 1000d);
			// System.out.println("FPS = " + (double) 1 / deltaTime);
			lastFrame = currentFrame;

			defaultShader.use();
			handleInputs(deltaTime, defaultShader);
			player.update(deltaTime);

			sl.setDirection(player.getCamera().getDirection());
			sl.setPosition(player.getCamera().getPosition());
			sl.apply(defaultShader, "spotLight");

			// render
			glClearColor(0.05f, 0.075f, 0.075f, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

			skybox.render(player.getCamera());
			defaultShader.use();

			glUniform3f(defaultShader.getUniform("viewPos"), player.getCamera().getPosition().x, player.getCamera().getPosition().y, player.getCamera().getPosition().z);

			Matrix4f view = player.getViewMatrix();
			glUniformMatrix4(defaultShader.getUniform("view"), false, view.getData());

			Matrix4f model = new Matrix4f();
			model.translate(new Vector3f(0, 0, -1));
			glUniformMatrix4(defaultShader.getUniform("model"), false, model.getData());

			bunny.render();

			// finish frame
			Display.update();
			Display.sync(500);
		}
	}

	public static boolean down = false;

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

		if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			if (!down) {
				down = true;
				defaultShader.updateParameter("SHININESS", Integer.valueOf(defaultShader.getParameter("SHININESS")) * 2);
				defaultShader.use();
			}
		} else {
			down = false;
		}

		// moving
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			Display.destroy();
			System.exit(0);
		}
	}
}
