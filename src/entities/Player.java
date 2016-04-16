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
import util.Shader;
import util.Util;

/**
 *
 * @author Erik
 */
public class Player {

	private static Mesh playerMesh;
	private static Material playerMat;

	private static Vector3f worldFront = new Vector3f(1, 0, 0);
	private static Vector3f worldNorth = new Vector3f(0, 1, 0);

	static {
		int dif = Util.loadTexture("container2.png");
		int spec = Util.loadTexture("container2_specular.png");
		playerMat = new Material(dif, spec);
		playerMesh = new Mesh("bird.obj");
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
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		viewDirAngle = 0;
		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
	}

	public void render(Shader shader) {
		model.setLocation(position);
		Vector3f rotationAxis = new Vector3f();
		rotationAxis = Vector3f.cross(worldNorth, position, rotationAxis);
		rotationAxis.normalise();

		Vector3f normPos = new Vector3f(position);
		normPos.normalise();

		float angle = (float) Math.acos(Vector3f.dot(normPos, worldNorth));

		Vector3f plainPos = new Vector3f(position.x, 0, position.z);
		plainPos.normalise();

		float baseRotationAngle = (float) Math.PI - (float) Math.acos(Vector3f.dot(plainPos, worldFront));

		Vector3f aequatorparallel = new Vector3f();
		aequatorparallel = Vector3f.cross(normPos, worldNorth, aequatorparallel);
		aequatorparallel.normalise();

		Vector3f nordDirection = new Vector3f();
		nordDirection = Vector3f.cross(normPos, aequatorparallel, nordDirection);
		nordDirection.normalise();

		float viewDirAngle = (float) Math.acos(Vector3f.dot(viewDir, nordDirection));

		Vector3f minustestvektor = Vector3f.cross(nordDirection, viewDir, null);
		minustestvektor.normalise();
		minustestvektor.scale(0.2f);
		Vector3f testPoint = Vector3f.add(normPos, minustestvektor, null);

		if (testPoint.length() < 1.0) {
			viewDirAngle *= -1;
		}

		if (Vector3f.cross(worldFront, plainPos, null).y > 0) {
			baseRotationAngle *= -1;
		}
		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngle, normPos);
		rot.rotate(angle, rotationAxis);
		rot.rotate(baseRotationAngle, worldNorth);

		model.setRotationMatrix(rot);
		model.setScale(new Vector3f(0.07f, 0.07f, 0.07f));
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
		// position = new Vector3f(0.1f, (float)
		// Math.sin(System.currentTimeMillis() % (int) (3000f * 2f * Math.PI) /
		// 3000f), (float) Math.cos(System.currentTimeMillis() % (int) (3000f *
		// 2f * Math.PI) / 3000f));
		// viewDirAngle += deltaTime;

		Vector3f prePos = new Vector3f(position);

		Vector3f normPos = new Vector3f(position);
		normPos.normalise();
		Matrix4f rot = new Matrix4f();

		rot.rotate(viewDirAngleDelta, normPos);

		viewDir = Util.vmMult(viewDir, rot);
		viewDir.normalise();

		float speed = dx * deltaTime * 0.15f;
		walk(speed, prePos);
	}

	private void walk(float speed, Vector3f prePos) {
		if (speed != 0) {
			Vector3f walkDir = new Vector3f(viewDir);
			walkDir.normalise();
			walkDir.scale(speed);
			position = Vector3f.add(position, walkDir, position);
			position.normalise();
			Vector3f positionDelta = new Vector3f();
			if (speed < 0) {
				positionDelta = Vector3f.sub(position, prePos, positionDelta);
			} else {
				positionDelta = Vector3f.sub(prePos, position, positionDelta);
			}
			Vector3f right = new Vector3f();
			right = Vector3f.cross(positionDelta, position, right);
			viewDir = Vector3f.cross(right, position, viewDir);
			viewDir.normalise();
		}
	}
}
