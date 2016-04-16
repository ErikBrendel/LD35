/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import util.Camera;
import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.Shader;
import util.Util;

/**
 *
 * @author Erik
 */
public class Player {

	private static Mesh playerMesh;
	private static Material playerMat;
	private static boolean btnDown = false;
	private static boolean btn2Down = false;

	private static Vector3f worldFront = new Vector3f(1, 0, 0);
	private static Vector3f worldNorth = new Vector3f(0, 1, 0);

	static {
		int dif = Util.loadTexture("container2.png");
		int spec = Util.loadTexture("container2_specular.png");
		playerMat = new Material(dif, spec);
		playerMesh = new Mesh("bunny.obj");
	}

	Camera camera;

	private Vector3f position;

	private Vector3f viewDir;

	private MeshInstance model;
	private float viewDirAngle;

	public Player(Vector3f position) {
		this.position = position;
		this.position.normalise();
		model = new MeshInstance(playerMesh, playerMat);
		viewDir = new Vector3f(1, 0, 0);
		viewDirAngle = 0;
	}

	public void render(Shader shader) {
		model.setLocation(position);
		Vector3f rotationAxis = new Vector3f();
		rotationAxis = Vector3f.cross(worldNorth, position, rotationAxis);

		Vector3f normPos = new Vector3f(position);
		normPos.normalise();

		float angle = (float) Math.acos(Vector3f.dot(normPos, worldNorth));

		Vector3f plainPos = new Vector3f(position.x, 0, position.z);
		plainPos.normalise();

		float baseRotationAngle = (float) Math.PI - (float) Math.acos(Vector3f.dot(plainPos, worldFront));

		if (Vector3f.cross(worldFront, plainPos, null).y > 0) {
			baseRotationAngle *= -1;
		}
		System.out.println(baseRotationAngle);
		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngle, position);
		rot.rotate(angle, rotationAxis);
		rot.rotate(baseRotationAngle, worldNorth);

		model.setRotationMatrix(rot);
		model.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
		model.render(shader);
	}

	public void teleportTo(Vector3f dest) {
		camera.setPosition(dest);
	}


	public Vector3f getPosition() {
		return position;
	}

	/**
	 * fetches input events (like lookaround and walking) and updates the
	 * uniform values to also show this progress
	 *
	 * @param deltaTime
	 *            time passed since last frame
	 */
	public void update(float deltaTime) {
		position = new Vector3f((float) Math.sin(System.currentTimeMillis() % (int) (3000f * 2f * Math.PI) / 3000f), 0.9f, (float) Math.cos(System.currentTimeMillis() % (int) (3000f * 2f * Math.PI) / 3000f));
		position.normalise();
		viewDirAngle += deltaTime;
		Vector3f right = new Vector3f();
		right = Vector3f.cross(worldNorth, position, right);
		Vector3f front = new Vector3f();
		front = Vector3f.cross(right, position, front);

		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngle, position);

		viewDir = Util.vmMult(front, rot);
		viewDir.normalise();

		// movement
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			
		}
	}
}
