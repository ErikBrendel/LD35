/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static util.ObjectLoader.loadObjectEBO;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import light.DirectionalLight;
import light.LightHandler;
import light.SpotLight;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import com.sun.prism.impl.BufferUtil;

import entities.Enemy;
import entities.Player;
import util.Camera;
import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.Scene;
import util.Shader;
import util.Skybox;
import util.Util;

/**
 * Main class for LD project
 */
public class SpaceScene implements Scene {

	private float deltaTime = 0;
	private long lastFrame = 0;
	private Player player;
	private Enemy enemy;
	private Camera camera;
	private SpotLight flashlight;
	private ArrayList<Shader> shaders;
	private LightHandler lh;
	private Shader defaultShader;
	private Shader noLightShader;
	private Shader instancedShader;
	private Vector3f sunPos;
	private Mesh asteroid;
	private int VAO;
	private int amount;
	private Material rockMat;
	private DirectionalLight sunLight;
	private Matrix4f projection;
	private Matrix4f view;
	private Skybox skybox;
	private int matricesUBO;

	private MeshInstance sun;
	private MeshInstance underwater;
	private MeshInstance land;
	private MeshInstance water;

	public SpaceScene() {
		Util.createWindow("Space explorer", false);

		shaders = new ArrayList<>();
		lh = new LightHandler();

		skybox = new Skybox("ownSky");

		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("SHININESS", 256);
		parameters.put("NUM_DIR_LIGHTS", 0);
		parameters.put("NUM_SPOT_LIGHTS", 0);
		parameters.put("NUM_POINT_LIGHTS", 0);
		defaultShader = Shader.fromFile("Basic.vert", "Basic.frag", parameters);
		noLightShader = Shader.fromFile("Basic.vert", "NoLight.frag");
		instancedShader = Shader.fromFile("Instanced.vert", "Basic.frag", parameters);

		defaultShader.use();
		defaultShader.addUniformBlockIndex(0, "Lights");
		defaultShader.addUniformBlockIndex(1, "Matrices");
		instancedShader.addUniformBlockIndex(0, "Lights");
		instancedShader.addUniformBlockIndex(1, "Matrices");
		noLightShader.addUniformBlockIndex(1, "Matrices");

		shaders.add(defaultShader);
		shaders.add(instancedShader);

		player = new Player(new Vector3f((float) Math.sqrt(2), (float) Math.sqrt(2), 0));

		enemy = new Enemy(new Vector3f(0, 1, 0), player, lh, shaders);

		camera = new Camera(new Vector3f(0, 0, 0), player.getPosition(), enemy.getPosition(), 3f);

		sunPos = new Vector3f(1, 1, 1);

		// light
		sunLight = new DirectionalLight(new Color(255, 255, 220), sunPos);

		flashlight = new SpotLight(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0f, 0.0f, -6.0f), new Vector3f(0, 0, 1), 10, 20, 15);

		lh.addLight(flashlight, shaders);
		lh.addLight(sunLight, shaders);

		matricesUBO = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
		glBufferData(GL_UNIFORM_BUFFER, 128, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, matricesUBO);
		projection = camera.getProjectionMatrix();
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projection.getData());
		view = camera.getViewMatrix();
		glBufferSubData(GL_UNIFORM_BUFFER, 64, view.getData());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		int cloud = Util.loadTexture("cloudSphere.png");
		int sunTex = Util.loadTexture("sun.jpg");
		int rock = Util.loadTexture("container2.png");
		int rockSpec = Util.loadTexture("container2_specular.png");
		int sandTex = Util.loadTexture("sand_color.png", false);
		int landTex = Util.loadTexture("green_color.png", false);
		int waterTex = Util.loadTexture("blue_color_alpha.png", false);
		int whiteTex = Util.loadTexture("white.png");
		int blackTex = Util.loadTexture("black.png");

		Material cloudMat = new Material(cloud, blackTex);
		Material sunMat = new Material(sunTex, 0);
		rockMat = new Material(rock, rockSpec);
		Material sandMat = new Material(sandTex, blackTex);
		Material landMat = new Material(landTex, blackTex);
		Material waterMat = new Material(waterTex, whiteTex);
		rockMat = new Material(rock, rockSpec);

		// planets
		Mesh planetSphere = loadObjectEBO("earth.obj");
		asteroid = loadObjectEBO("asteroid.obj");
		Mesh underwaterMesh = loadObjectEBO("gamePlanetUnderwater.obj");
		Mesh landMesh = loadObjectEBO("gamePlanetLand.obj");
		Mesh waterMesh = loadObjectEBO("gamePlanetWater.obj");

		underwater = new MeshInstance(underwaterMesh, sandMat);
		float worldScale = 1f / 1.015f;
		Vector3f scaleVec = new Vector3f(worldScale, worldScale, worldScale);
		underwater.setScale(scaleVec);
		land = new MeshInstance(landMesh, landMat);
		land.setScale(scaleVec);
		water = new MeshInstance(waterMesh, waterMat);
		water.setScale(scaleVec);

		sun = new MeshInstance(planetSphere, sunMat);
		sun.setScale(new Vector3f(5, 5, 5));

		amount = 1000;
		Matrix4f[] matrices = new Matrix4f[amount];
		Random ran = new Random(System.currentTimeMillis());
		float radius = 8;
		float offset = 0.5f;
		for (int i = 0; i < amount; i++) {
			Matrix4f model = new Matrix4f();
			float angle = (float) i / amount * 360f;
			float displacement = ran.nextInt() % (offset * 200) / 100f - offset;
			float x = (float) Math.sin(Math.toDegrees(angle)) * radius + displacement;
			displacement = ran.nextInt() % (int) (2 * offset * 100) / 100.0f - offset;
			float y = displacement * 0.4f; // Keep height of asteroid
											// field smaller compared to
											// width of x and z
			displacement = ran.nextInt() % (int) (2 * offset * 100) / 100.0f - offset;
			float z = (float) Math.cos(Math.toDegrees(angle)) * radius + displacement;
			model.translate(new Vector3f(x, y, z));

			// 2. Scale: Scale between 0.05 and 0.25f
			float scale = ran.nextInt() % 50 / 100.0f + 0.25f;
			model.scale(new Vector3f(scale, scale, scale));

			// 3. Rotation: add random rotation around a (semi)randomly picked
			// rotation axis vector
			float rotAngle = ran.nextInt() % 360;
			model.rotate((float) Math.toDegrees(rotAngle), new Vector3f(0.4f, 0.6f, 0.8f));

			matrices[i] = model;
		}

		FloatBuffer data = BufferUtil.newFloatBuffer(amount * 16);
		for (int i = 0; i < amount; i++) {
			data.put(matrices[i].getDataArray());
		}
		data.flip();

		VAO = asteroid.getVAO();
		glBindVertexArray(VAO);
		int buffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glEnableVertexAttribArray(3);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 64, 0);
		glEnableVertexAttribArray(4);
		glVertexAttribPointer(4, 4, GL_FLOAT, false, 64, 16);
		glEnableVertexAttribArray(5);
		glVertexAttribPointer(5, 4, GL_FLOAT, false, 64, 32);
		glEnableVertexAttribArray(6);
		glVertexAttribPointer(6, 4, GL_FLOAT, false, 64, 48);

		glVertexAttribDivisor(3, 1);
		glVertexAttribDivisor(4, 1);
		glVertexAttribDivisor(5, 1);
		glVertexAttribDivisor(6, 1);

		glBindVertexArray(0);

		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		// game loop
	}

	public void start() {
		while (!Display.isCloseRequested()) {
			// create window

			// get deltaTime and FPS
			long currentFrame = System.nanoTime();
			deltaTime = (float) ((currentFrame - lastFrame) / 1000000d / 1000d);
			// System.out.println("FPS = " + (double) 1 / deltaTime);
			lastFrame = currentFrame;

			// enemy.setPosition(new Vector3f((float)
			// Math.sin(System.currentTimeMillis() % (int) (3000f * 2f *
			// Math.PI) / 3000f), 0.1f, (float)
			// Math.cos(System.currentTimeMillis() % (int) (3000f * 2f *
			// Math.PI) / 3000f)));
			camera.setWorldView(new Vector3f(0.0f, 0.0f, 0.0f), player.getPosition(), enemy.getPosition());
			// handle all inputs
			sunPos = new Vector3f((float) -Math.sin(lastFrame / 10000000000d) * 50, 4, (float) Math.cos(lastFrame / 10000000000d) * 50);
			Vector3f sunDir = new Vector3f(-sunPos.x, -sunPos.y, -sunPos.z);
			sunLight.setDirection(sunDir);
			defaultShader.use();
			handleInputs(deltaTime, defaultShader);
			player.update(deltaTime);
			player.setNearest(land.getMesh());
			enemy.update(deltaTime);

			// Update Matrices Uniform Buffer Block
			view = camera.getViewMatrix();
			projection = camera.getProjectionMatrix();

			glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
			glBufferSubData(GL_UNIFORM_BUFFER, 0, projection.getData());
			glBufferSubData(GL_UNIFORM_BUFFER, 64, view.getData());
			glBindBuffer(GL_UNIFORM_BUFFER, 0);

			Vector3f flashLightDirection = new Vector3f(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
			flashlight.setDirection(flashLightDirection);
			flashlight.setPosition(camera.getPosition());

			lh.updateLight(flashlight);
			lh.updateLight(sunLight);

			render();

			// finish frame
			Display.update();
			Display.sync(600);
		}
	}

	@Override
	public void render() {
		defaultShader.use();
		glClearColor(0.05f, 0.075f, 0.075f, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		glUniform1i(defaultShader.getUniform("skybox"), skybox.getTexture());
		glUniform1i(defaultShader.getUniform("alpha"), 1);
		// update player position uniform
		camera.apply(defaultShader);

		// earth
		// float angle = (float) (System.currentTimeMillis() % (1000 * 360 *
		// Math.PI)) / 5000f / 2f;
		// Vector3f rot = new Vector3f(0, angle, 0);
		underwater.render(defaultShader);
		land.render(defaultShader);

		// player
		player.render(defaultShader);
		enemy.render(defaultShader);

		water.render(defaultShader);

		// sun
		sun.setLocation(sunPos);
		noLightShader.use();
		sun.render(noLightShader);

		instancedShader.use();
		camera.apply(instancedShader);
		rockMat.apply(instancedShader);
		glBindVertexArray(VAO);
		glDrawElementsInstanced(GL_TRIANGLES, asteroid.getVertCount(), GL_UNSIGNED_INT, 0, amount);
		glBindVertexArray(0);

		// skybox
		skybox.render(camera);
	}

	public boolean down = false;

	private void handleInputs(float deltaTime, Shader defaultShader) {
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
		/*
		 * player.getCamera().processMouseScroll(scrollDelta * deltaTime); if
		 * (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
		 * player.getCamera().processMouseScroll(-60 * deltaTime); } if
		 * (Keyboard.isKeyDown(Keyboard.KEY_E)) {
		 * player.getCamera().processMouseScroll(60 * deltaTime); }
		 * 
		 * if (Keyboard.isKeyDown(Keyboard.KEY_Y)) { player.getCamera().roll(1 *
		 * deltaTime); } if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
		 * player.getCamera().roll(-1 * deltaTime); }
		 */
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
