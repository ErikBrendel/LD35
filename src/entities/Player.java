/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.Util;

/**
 *
 * @author Erik
 */
public class Player extends WorldObject {

	private static final Mesh eagleMesh;
	private static final Material eagleMat;
	private static final Mesh sharkMesh;
	private static final Material sharkMat;

	static {
		int eagle = Util.loadTexture("bird.png");
		int shark = Util.loadTexture("shark.jpg");
		int spec = Util.loadTexture("black.png");
		eagleMat = new Material(eagle, spec);
		sharkMat = new Material(shark, spec);
		eagleMesh = new Mesh("bird.obj");
		sharkMesh = new Mesh("shark.obj");
	}

	private Vector3f neighbour;
	private float speed;

	public Player(Vector3f position) {
		super(new MeshInstance(eagleMesh, eagleMat), new MeshInstance(sharkMesh, sharkMat, false));
		model[0].setScale(new Vector3f(0.07f, 0.07f, 0.07f));
		model[1].setScale(new Vector3f(0.12f, 0.12f, 0.12f));
		this.position = position;
		this.position.normalise();
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
		speed = 0.15f;
	}

	/**
	 * fetches input events (like lookaround and walking) and updates the
	 * uniform values to also show this progress
	 *
	 * @param deltaTime
	 *            time passed since last frame
	 */

	public void setNearest(Mesh m) {
		neighbour = m.getNearestVertex(position);

		System.out.println(neighbour.length());

		if (neighbour.length() < 1.01f) {
			model[0].setVisible(false);
			model[1].setVisible(true);
			speed = 0.15f;
		} else {
			model[0].setVisible(true);
			model[1].setVisible(false);
			speed = 0.15f;
		}
	}

	public void update(float deltaTime) {

		int dx = 0, dy = 0;
		// movement
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			dx--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			dx++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			dy--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			dy++;

		}
		float viewDirAngleDelta = dy * deltaTime * 2f;

		Vector3f prePos = getPosition();

		Vector3f normPos = getPosition();
		normPos.normalise();

		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngleDelta, normPos);
		viewDir = Util.vmMult(viewDir, rot);
		viewDir.normalise();

		float s = dx * deltaTime * speed;

		walk(s, prePos);
	}
}
