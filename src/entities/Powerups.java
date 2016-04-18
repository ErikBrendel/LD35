/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import light.LightHandler;
import light.PointLight;
import main.Balancing;
import main.SpaceScene;

import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.ObjectLoader;
import util.Shader;

/**
 *
 * @author Erik
 */
public class Powerups {

	private static final Mesh flashMesh;
	private static final Material flashMat;

	static {
		flashMesh = ObjectLoader.loadObjectEBO("powerup.obj");
		flashMat = new Material("white.png", "white.png");
	}

	private ArrayList<PowerupInstance> instances;

	LightHandler lights;

	public Powerups(LightHandler lights) {
		super();
		this.lights = lights;
		instances = new ArrayList<>();
	}

	public void update(float deltaTime, Player player, ArrayList<Shader> shaders) {
		Random r = new Random();
		// update each one
		for (PowerupInstance o : instances) {
			o.update(deltaTime);
		}

		// spawning new ones
		if (r.nextFloat() < deltaTime * 2 && instances.size() < 10) {
			spawnNew(r, shaders);
		}

		// despawn existing ones
		if (player.getCurrentMesh() != 1) {
			Vector3f playerPos = player.getPosition();
			for (int i = 0; i < instances.size(); i++) {
				PowerupInstance inst = instances.get(i);
				float dist = (float) Math.hypot(Math.hypot(inst.position.x - playerPos.x, inst.position.y - playerPos.y), inst.position.z - playerPos.z);
				if (dist < Balancing.getPowerupPickupDistance()) {
					lights.remLight(inst.getLight(), shaders);
					instances.remove(inst);
					i--;
					player.setPowerup(Balancing.getPowerupStrength());
					SpaceScene.playSound("e_powerup");
				}
			}
		}
	}

	public void render(Shader shader) {
		for (PowerupInstance o : instances) {
			o.render(shader);
		}
	}

	public void spawnNew(Random r, ArrayList<Shader> shaders) {

		Vector3f pos = new Vector3f(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f);
		pos.normalise();
		MeshInstance model = new MeshInstance(flashMesh, flashMat);
		model.setLocation(pos);
		model.setScale(0.1f);
		PowerupInstance wo = new PowerupInstance(pos, model);
		instances.add(wo);
		lights.addLight(wo.getLight(), shaders);
	}

	private static class PowerupInstance extends WorldObject {

		private float age;
		private PointLight pl;

		public PowerupInstance(Vector3f pos, MeshInstance... model) {
			super(model);
			position = pos;
			viewDir = WORLD_FRONT;
			age = 0;
			Vector3f lightpos = new Vector3f(position);
			lightpos.scale(1.03f);
			pl = new PointLight(new Color(1, 0.5f, 0.0f), lightpos, 1f);
		}

		public PointLight getLight() {
			return pl;
		}

		public void update(float deltaTime) {
			age += deltaTime;

			float sin = ((float) Math.sin(age * 5f) + 1f) * 0.3f;
			float rot = age;

			Matrix4f local = new Matrix4f();
			local.translate(new Vector3f(0, sin, 0));
			local.rotate(rot, new Vector3f(0, 1, 0));

			modelMatrix[0] = local;
		}

	}

}
