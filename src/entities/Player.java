/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import main.Balancing;
import main.SpaceScene;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import particles.ParticleHandler;
import particles.ShapeShiftParticle;
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
	private static final Mesh particleMesh;
	private static final Material particleMat;

	static {
		int eagle = Util.loadTexture("bird.png");
		int shark = Util.loadTexture("shark.jpg");
		int spec = Util.loadTexture("black.png");
		int white = Util.loadTexture("white.png");
		int leopard = Util.loadTexture("leopard.jpg");
		eagleMat = new Material(eagle, spec);
		sharkMat = new Material(shark, spec);
		leopardMat = new Material(leopard, white);
		particleMat = new Material(white, white);
		leopardMesh = ObjectLoader.loadObjectEBO("leopard_body.obj");
		leopardLegMesh = ObjectLoader.loadObjectEBO("leopard_leg.obj");
		eagleMesh = ObjectLoader.loadObjectEBO("bird.obj");
		sharkMesh = ObjectLoader.loadObjectEBO("shark.obj");
		particleMesh = ObjectLoader.loadObjectEBO("particle.obj");
	}

	private Vector3f neighbour;
	private float relativeSpeed;
	private int currentMesh;
	private int nextMesh;
	private float timeAnimating;
	private float[] scales;
	private boolean overLand;
	private float leoLegAnimProgress = 0f;
	private ParticleHandler particles;
	private float powerup;

	public Player(Vector3f position, Shader instanceShader) {
		super(new MeshInstance(eagleMesh, eagleMat), new MeshInstance(sharkMesh, sharkMat, false), new MeshInstance(leopardMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false), new MeshInstance(leopardLegMesh, leopardMat, false));

		particles = new ParticleHandler(position, new ShapeShiftParticle(), particleMesh, particleMat, instanceShader);

		scales = new float[model.length];
		scales[0] = 0.07f;
		scales[1] = 0.12f;
		scales[2] = 0.02f;
		for (int i = 0; i < model.length; i++) {
			model[i].setScale(scales[Math.min(i, 2)]);
		}
		this.position = position;
		this.position.normalise();
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
		relativeSpeed = 1f;
		powerup = 0;
	}

	public void setNearest(Mesh m) {
		relativeSpeed = 1;
		if (currentMesh == nextMesh) {
			neighbour = m.getNearestVertex(position);

			if (neighbour.length() < 0.99f) {
				overLand = false;
				if (currentMesh == 0) {
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 1;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
				}
				if (currentMesh == 1) {
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 0;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
				}
				if (currentMesh == 2) {
					relativeSpeed = 0.07f;
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 0;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 1;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
				}
			} else {
				overLand = true;
				if (currentMesh == 0) {
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 2;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
				}
				if (currentMesh == 1) {
					relativeSpeed = 0.02f;
					if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
						nextMesh = 0;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 2;
						SpaceScene.playSound("e_shapeshift");
						return;
					}
				}
				if (currentMesh == 2) {
					if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
						nextMesh = 0;
						SpaceScene.playSound("e_shapeshift");
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
			particles.setOrigin(position);
			particles.emit(100);
			timeAnimating += deltaTime;
			if (timeAnimating < Balancing.getTimeModelShrinking()) {
				model[currentMesh].setScale(scales[currentMesh] * interpolate((Balancing.getTimeModelShrinking() - timeAnimating) / Balancing.getTimeModelShrinking()));
			} else if (timeAnimating < Balancing.getTimeModelExpanding() + Balancing.getTimeModelShrinking()) {
				model[currentMesh].setVisible(false);
				model[currentMesh].setScale(scales[currentMesh]);
				model[nextMesh].setVisible(true);
				model[nextMesh].setScale(scales[nextMesh] * interpolate((timeAnimating - Balancing.getTimeModelShrinking()) / Balancing.getTimeModelExpanding()));
			} else {
				model[nextMesh].setScale(scales[nextMesh]);
				currentMesh = nextMesh;
				timeAnimating = 0;
				relativeSpeed = 1;
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

		//walkSpeed = [-1,1]:keyPress * frame * stayStuck * powerup * balanced base speed
		float walkSpeed = dx * deltaTime * relativeSpeed * (1 + powerup) * Balancing.getPlayerSpeed(currentMesh);
		powerup *= 1 - deltaTime;

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
		particles.update(deltaTime);
	}

	public void renderParticles() {
		particles.render();
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

	public void setPowerup(float powerup) {
		this.powerup += powerup;
	}

}
