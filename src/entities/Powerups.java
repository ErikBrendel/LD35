/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package entities;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.text.Position;
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

	public Powerups() {
		super();
		instances = new ArrayList<>();
	}

	public void update(float deltaTime, Player player) {
		Random r = new Random();
		//update each one
		for (PowerupInstance o : instances) {
			o.update(deltaTime);
		}

		//spawning new ones
		if (r.nextFloat() < deltaTime * 2 && instances.size() < 20) {
			spawnNew(r);
		}

		//despawn existing ones
		if (player.getCurrentMesh() != 1) {
			Vector3f playerPos = player.getPosition();
			for (int i = 0; i < instances.size(); i++) {
				PowerupInstance inst = instances.get(i);
				float dist = (float) Math.hypot(Math.hypot(inst.position.x - playerPos.x, inst.position.y - playerPos.y), inst.position.z - playerPos.z);
				if (dist < 0.04) {
					instances.remove(inst);
					i--;
					player.setPowerup(2);
				}
			}
		}
	}

	public void render(Shader shader) {
		for (PowerupInstance o : instances) {
			o.render(shader);
		}
	}

	public void spawnNew(Random r) {

		Vector3f pos = new Vector3f(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f);
		pos.normalise();
		MeshInstance model = new MeshInstance(flashMesh, flashMat);
		model.setLocation(pos);
		model.setScale(0.1f);
		PowerupInstance wo = new PowerupInstance(pos, model);
		instances.add(wo);

	}

	private static class PowerupInstance extends WorldObject {

		private float age;

		public PowerupInstance(Vector3f pos, MeshInstance... model) {
			super(model);
			position = pos;
			viewDir = WORLD_FRONT;
			age = 0;
		}

		public void update(float deltaTime) {
			age += deltaTime;

			float sin = ((float) Math.sin(age * 5f) + 1f) * 0.3f;

			Matrix4f local = new Matrix4f();
			local.translate(new Vector3f(0, sin, 0));

			modelMatrix[0] = local;
		}

	}

}
