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

import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.Player;
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
	private SpotLight flashlight;
	private ArrayList<Shader> shaders;
	private LightHandler lh;
	private Shader defaultShader;
	private Shader noLightShader;
	private Shader instancedShader;
	private MeshInstance earth;
	private MeshInstance clouds;
	private MeshInstance sun;
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

	public SpaceScene() {
		Util.createWindow("Space explorer", true);

		player = new Player();

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

		matricesUBO = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
		glBufferData(GL_UNIFORM_BUFFER, 128, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, matricesUBO);
		projection = player.getProjectionMatrix();
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projection.getData());
		view = player.getViewMatrix();
		glBufferSubData(GL_UNIFORM_BUFFER, 64, view.getData());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		shaders.add(defaultShader);
		shaders.add(instancedShader);

		int dif = Util.loadTexture("earth.jpg");
		int spec = Util.loadTexture("earth_spec.jpg");
		int cloud = Util.loadTexture("cloudSphere.png");
		int sunTex = Util.loadTexture("sun.jpg");
		int rock = Util.loadTexture("container2.png");
		int rockSpec = Util.loadTexture("container2_specular.png");

		Material earthMat = new Material(dif, spec);
		Material cloudMat = new Material(cloud, 0);
		Material sunMat = new Material(sunTex, 0);
		rockMat = new Material(rock, rockSpec);

		sunPos = new Vector3f(1, 1, 1);

		// light
		sunLight = new DirectionalLight(new Vector3f(1, 1, 1), sunPos);

		flashlight = new SpotLight(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0f, 0.0f, -6.0f), new Vector3f(0, 0, 1), 10, 20, 15);

		lh.addLight(flashlight, shaders);
		lh.addLight(sunLight, shaders);

		// planets
		Mesh planetSphere = loadObjectEBO("earth.obj");
		asteroid = loadObjectEBO("asteroid.obj");

		earth = new MeshInstance(planetSphere, earthMat);
		earth.setLocation(new Vector3f(0, 0, 0));

		clouds = new MeshInstance(planetSphere, cloudMat);
		clouds.setLocation(new Vector3f(0, 0, 0));
		float cloudScale = 1.01f;
		clouds.setScale(new Vector3f(cloudScale, cloudScale, cloudScale));

		sun = new MeshInstance(planetSphere, sunMat);
		sun.setScale(new Vector3f(5, 5, 5));

		amount = 100000;
		Matrix4f[] matrices = new Matrix4f[amount];
		Random ran = new Random(System.currentTimeMillis());
		float radius = 10;
		float offset = 10.5f;
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
			float scale = ran.nextInt() % 20 / 10.0f + 0.05f;
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
			glClearColor(0.05f, 0.075f, 0.075f, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

			// get deltaTime and FPS
			long currentFrame = System.nanoTime();
			deltaTime = (float) ((currentFrame - lastFrame) / 1000000d / 1000d);
			// System.out.println("FPS = " + (double) 1 / deltaTime);
			lastFrame = currentFrame;

			// handle all inputs
			sunPos = new Vector3f((float) -Math.sin(lastFrame / 10000000000d) * 50, 4, (float) Math.cos(lastFrame / 10000000000d) * 50);
			sunLight.setDirection(sunPos);
			defaultShader.use();
			handleInputs(deltaTime, defaultShader);
			player.update(deltaTime);

			// Update Matrices Uniform Buffer Block
			view = player.getViewMatrix();
			projection = player.getProjectionMatrix();

			glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
			glBufferSubData(GL_UNIFORM_BUFFER, 0, projection.getData());
			glBufferSubData(GL_UNIFORM_BUFFER, 64, view.getData());
			glBindBuffer(GL_UNIFORM_BUFFER, 0);

			flashlight.setDirection(player.getCamera().getDirection());
			flashlight.setPosition(player.getCamera().getPosition());

			lh.updateLight(flashlight);
			lh.updateLight(sunLight);

			Shader[] shaders = new Shader[3];
			shaders[0] = defaultShader;
			shaders[1] = noLightShader;
			shaders[2] = instancedShader;

			shaders[0].use();
			glUniform1i(shaders[0].getUniform("alpha"), 1);
			// update player position uniform
			player.applyToShader(shaders[0], true);

			// earth
			float angle = (float) (System.currentTimeMillis() % (1000 * 360 * Math.PI)) / 5000f / 2f;
			earth.setRotation(new Vector3f(0, angle, 0));

			// clouds
			clouds.setRotation(new Vector3f(0, angle * 1f, 0));

			// sun
			sun.setLocation(sunPos);
			shaders[1].use();
			player.applyToShader(shaders[1], false);
			shaders[0].use();

			shaders[2].use();
			rockMat.apply(shaders[2]);

			render(shaders);
			lh.renderLightShadows(this);

			// skybox
			glUniform1i(defaultShader.getUniform("skybox"), skybox.getTexture());
			// skybox.render(player.getCamera());

			// finish frame
			Display.update();
			Display.sync(60);
		}
	}

	@Override
	public void render(Shader[] shaders) {

		earth.render(shaders[0]);
		clouds.render(shaders[0]);
		sun.render(shaders[1]);
		// shaders[2].use();
		// glBindVertexArray(VAO);
		// glDrawElementsInstanced(GL_TRIANGLES, asteroid.getVertCount(),
		// GL_UNSIGNED_INT, 0, amount);
		// glBindVertexArray(0);
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
