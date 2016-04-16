/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import util.Camera;
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

	private static final Mesh playerMesh;
	private static final Material playerMat;

	static {
		int dif = Util.loadTexture("container2.png");
		int spec = Util.loadTexture("container2_specular.png");
		playerMat = new Material(dif, spec);
		playerMesh = new Mesh("bird.obj");
	}

	Camera camera;

	public Player(Vector3f position) {
		super(new MeshInstance(playerMesh, playerMat));
		model[0].setScale(new Vector3f(0.07f, 0.07f, 0.07f));
		this.position = position;
		this.position.normalise();
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
	}

	/**
	 * fetches input events (like lookaround and walking) and updates the
	 * uniform values to also show this progress
	 *
	 * @param deltaTime
	 *            time passed since last frame
	 */
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

		float speed = dx * deltaTime * 0.15f;
		walk(speed, prePos);
	}
}
