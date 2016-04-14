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
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthFunc;
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
import java.util.ArrayList;
import java.util.HashMap;

import light.DirectionalLight;
import light.LightHandler;
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
import util.MeshInstance;
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
	static SpotLight flashlight;
	static ArrayList<Shader> shaders;
	static LightHandler lh;

	public static void main(String[] args) {

		// create window
		Util.createWindow("Space explorer", true);

		shaders = new ArrayList<>();
		lh = new LightHandler();

		Skybox skybox = new Skybox("ownSky");

		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("SHININESS", 64);
		parameters.put("NUM_DIR_LIGHTS", 0);
		parameters.put("NUM_SPOT_LIGHTS", 0);
		parameters.put("NUM_POINT_LIGHTS", 0);
		Shader defaultShader = Shader.fromFile("Basic.vert", "Basic.frag", parameters);

		defaultShader.use();
		defaultShader.addUniformBlockIndex(0, "Lights");
		shaders.add(defaultShader);

		player = new Player();

		int dif = Util.loadTexture("earth.jpg");
		int spec = Util.loadTexture("earth_spec.jpg");
		int cloud = Util.loadTexture("cloudSphere.png");
		int sunTex = Util.loadTexture("sun.jpg");

		Material earthMat = new Material(dif, spec);
		Material cloudMat = new Material(cloud, 0);
		Material sunMat = new Material(sunTex, 0);

		earthMat.apply(defaultShader);

		// light
		DirectionalLight sunLight = new DirectionalLight(new Color(255, 255, 220), new Vector3f(2, -1, 2));

		flashlight = new SpotLight(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0f, 0.0f, -6.0f), new Vector3f(0, 0, 1), 10, 20, 15);

		PointLight pl = new PointLight(Color.ORANGE, new Vector3f(0, 0, 4), 30);

		lh.addLight(flashlight, shaders);
		lh.addLight(flashlight, shaders);
		lh.addLight(pl, shaders);
		lh.addLight(sunLight, shaders);

		// planets
		Mesh planetSphere = loadObjectEBO("earth.obj");
		
		MeshInstance earth = new MeshInstance(planetSphere, earthMat);
		earth.setLocation(new Vector3f(0, 0, -2));
		
		MeshInstance clouds = new MeshInstance(planetSphere, cloudMat);
		clouds.setLocation(new Vector3f(0, 0, -2));
		float cloudScale = 1.01f;
		clouds.setScale(new Vector3f(cloudScale, cloudScale, cloudScale));
		
		MeshInstance sun = new MeshInstance(planetSphere, sunMat);
		sun.setScale(new Vector3f(5, 5, 5));

		Vector3f sunPos, earthPos = new Vector3f(0, 0, 0);

		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		// game loop
		while (!Display.isCloseRequested()) {
			// get deltaTime and FPS
			long currentFrame = System.nanoTime();
			deltaTime = (float) ((currentFrame - lastFrame) / 1000000d / 1000d);
			System.out.println("FPS = " + (double) 1 / deltaTime);
			lastFrame = currentFrame;

			// handle all inputs
			sunPos = new Vector3f((float) -Math.sin(lastFrame / 10000000000d) * 50, 4, (float) Math.cos(lastFrame / 10000000000d) * 50);
			sunLight.setDirection(Vector3f.sub(earthPos, sunPos, null));
			defaultShader.use();
			handleInputs(deltaTime, defaultShader);
			player.update(deltaTime);

			flashlight.setDirection(player.getCamera().getDirection());
			flashlight.setPosition(player.getCamera().getPosition());

			lh.updateLight(flashlight);
			lh.updateLight(sunLight);

			// render init and background
			defaultShader.use();
			glClearColor(0.05f, 0.075f, 0.075f, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

			glUniform1i(defaultShader.getUniform("alpha"), 1);
			// update player position uniform
			player.applyToShader(defaultShader);

			// earth
			float angle = (float) (System.currentTimeMillis() % (1000 * 360 * Math.PI)) / 5000f / 2f;
			earth.setRotation(new Vector3f(0, angle, 0));
			earth.render(defaultShader);

			// clouds
			clouds.setRotation(new Vector3f(0, angle * 0.2f, 0));
			clouds.render(defaultShader);
			
			// sun
			sun.setLocation(sunPos);
			sun.render(defaultShader);

			// skybox
			glUniform1i(defaultShader.getUniform("skybox"), skybox.getTexture());
			skybox.render(player.getCamera());

			// finish frame
			Display.update();
			Display.sync(500);
		}
	}

	public static boolean down = false;

	private static void handleInputs(float deltaTime, Shader defaultShader) {
		Mouse.poll();
		if (Mouse.isButtonDown(0)) {
			flashlight.setColor(new Vector3f(1, 1, 1));
		} else {
			flashlight.setColor(new Vector3f(0, 0, 0));
		}
		// flashlight.apply(defaultShader, null);

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

		if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
			player.getCamera().roll(1 * deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			player.getCamera().roll(-1 * deltaTime);
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
