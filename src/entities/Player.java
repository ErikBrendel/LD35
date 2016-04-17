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
import util.ObjectLoader;
import util.Shader;
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
	private static final Mesh leopardMesh;
	private static final Mesh leopardLegMesh;
	private static final Material leopardMat;
	private static final Mesh transitionkMesh;
	private static final Material transitionkMat;
	private static final float timeModelShrinking = 0.8f;
	private static final float offsetSphereExpanding = 0.4f;
	private static final float timeSphereExpanding = 0.5f;
	private static final float timeSphereShrinking = 0.5f;

	static {
		int eagle = Util.loadTexture("bird.png");
		int shark = Util.loadTexture("shark.jpg");
		int spec = Util.loadTexture("black.png");
		int white = Util.loadTexture("white.png");
		int leopard = Util.loadTexture("leopard.jpg");
		eagleMat = new Material(eagle, spec);
		sharkMat = new Material(shark, spec);
		transitionkMat = new Material(white, white);
		leopardMat = new Material(leopard, white);
		leopardMesh = ObjectLoader.loadObjectEBO("leopard_body.obj");
		leopardLegMesh = ObjectLoader.loadObjectEBO("leopard_leg.obj");
		eagleMesh = ObjectLoader.loadObjectEBO("bird.obj");
		sharkMesh = ObjectLoader.loadObjectEBO("shark.obj");
		transitionkMesh = ObjectLoader.loadObjectEBO("shapeshiftoverlay.obj");
	}

	private Vector3f neighbour;
	private float speed;
	private int currentMesh;
	private int nextMesh;
	private float timeAnimating;
	private float[] scales;
	private float[] speeds;
	private MeshInstance transition;
	private boolean overLand;
	private float leoLegAnimProgress = 0f;

	public Player(Vector3f position) {
		super(new MeshInstance(eagleMesh, eagleMat), new MeshInstance(sharkMesh, sharkMat, false), new MeshInstance(leopardMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false));

		transition = new MeshInstance(transitionkMesh, transitionkMat);
		transition.setVisible(false);
		scales = new float[model.length];
		speeds = new float[model.length];
		scales[0] = 0.07f;
		scales[1] = 0.12f;
		scales[2] = 0.02f;
		speeds[0] = 0.14f;
		speeds[1] = 0.20f;
		speeds[2] = 0.17f;
		for (int i = 0; i < model.length; i++) {
			model[i].setScale(scales[Math.min(i, 2)]);
		}
		this.position = position;
		this.position.normalise();
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
		speed = 0.15f;
	}

	public void setNearest(Mesh m) {
		speed = speeds[currentMesh];
		if (currentMesh == nextMesh) {
			neighbour = m.getNearestVertex(position);

			if (neighbour.length() < 0.99f) {
				overLand = false;
				if (currentMesh == 0) {
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 1;
						return;
					}
				}
				if (currentMesh == 1) {
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 0;
						return;
					}
				}
				if (currentMesh == 2) {
					speed = 0.03f;
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 0;
						return;
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 1;
						return;
					}
				}
			} else {
				overLand = true;
				if (currentMesh == 0) {
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 2;
						return;
					}
				}
				if (currentMesh == 1) {
					speed = 0.01f;
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 0;
						return;
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 2;
						return;
					}
				}
				if (currentMesh == 2) {
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 0;
						return;
					}
				}
			}
		}
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

		if (nextMesh != currentMesh) {
			timeAnimating += deltaTime;
			if (timeAnimating < timeModelShrinking) {
				model[currentMesh].setScale(scales[currentMesh] * interpolate((timeModelShrinking - timeAnimating) / timeModelShrinking));
			}
			if (timeAnimating > offsetSphereExpanding && timeAnimating < offsetSphereExpanding + timeSphereExpanding) {
				float timeExpanding = timeAnimating - offsetSphereExpanding;
				transition.setVisible(true);
				transition.setScale(interpolate(1 - (timeSphereExpanding - timeExpanding) / timeSphereExpanding) * 0.2f);
				Matrix4f rot = new Matrix4f();
				Vector3f axis = new Vector3f(0.7f, 0.7f, timeExpanding * 3f);
				axis.normalise();
				rot.rotate(timeExpanding * 4, axis);
				transition.setRotationMatrix(rot);
			} else if (timeAnimating > offsetSphereExpanding && timeAnimating < offsetSphereExpanding + timeSphereExpanding + timeSphereShrinking) {
				float timeExpanding = timeAnimating - offsetSphereExpanding;
				float timeRetreating = timeAnimating - offsetSphereExpanding - timeSphereExpanding;
				transition.setVisible(true);
				transition.setScale(interpolate((timeSphereExpanding - timeRetreating) / timeSphereExpanding) * 0.2f);
				Matrix4f rot = new Matrix4f();
				Vector3f axis = new Vector3f(0.7f, 0.7f, timeExpanding * 3f);
				axis.normalise();
				rot.rotate(timeExpanding * 4, axis);
				transition.setRotationMatrix(rot);

				model[currentMesh].setVisible(false);
				model[currentMesh].setScale(scales[currentMesh]);
				model[nextMesh].setVisible(true);
			} else if (timeAnimating > offsetSphereExpanding) {
				transition.setVisible(false);
				currentMesh = nextMesh;
				timeAnimating = 0;
				speed = speeds[currentMesh];
			}

			for (int m = 3; m < 7; m++) {
				model[m].setScale(model[2].getScale());
				model[m].setVisible(model[2].isVisible());
			}
		} else {
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
		}

		// walking
		float viewDirAngleDelta = dy * deltaTime * 2f;

		Vector3f prePos = getPosition();

		Vector3f normPos = getPosition();
		normPos.normalise();

		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngleDelta, normPos);
		viewDir = Util.vmMult(viewDir, rot);
		viewDir.normalise();

		float walkSpeed = dx * deltaTime * speed;

		walk(walkSpeed, prePos);

		// animate leopard legs
		if (currentMesh == 2 || nextMesh == 2) {
			leoLegAnimProgress += deltaTime * 1.5f * (dx | dy);
			leoLegAnimProgress %= 1f;

			for (int leg = 0; leg < 4; leg++) {
				float legRot = (float) Math.sin((leoLegAnimProgress - leg * 0.2f) * Math.PI * 2) * 0.9f;

				float legdX = leg >= 2 ? 1f : -1f;
				float legdZ = leg % 2 == 0 ? 0.25f : -0.25f;
				float legdY = 1.5f;

				Matrix4f legRotMatrix = new Matrix4f();
				legRotMatrix.translate(new Vector3f(legdX, legdY, legdZ));
				legRotMatrix.rotate(legRot, new Vector3f(0, 0, 1));
				modelMatrix[3 + leg] = legRotMatrix;
			}

		}
	}

	@Override
	public void render(Shader shader) {
		super.render(shader);
		transition.setLocation(position);
		transition.render(shader);
	}

	public int getCurrentMesh() {
		return currentMesh;
	}

	public int getNextMesh() {
		return nextMesh;
	}

	public boolean isOverland() {
		return overLand;
	}

	private float interpolate(float value) {
		return (float) (-2 * Math.pow(value, 3) + 3 * Math.pow(value, 2));
	}
}
