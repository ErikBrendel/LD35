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
import util.Mesh;

/**
 *
 * @author Erik
 */
public class Player {

	private static Mesh playerMesh;

	static {
		playerMesh = new Mesh("bunny.obj");

	}

	Camera camera;
	private ArrayList<ClickHandler> handlers;
	static boolean btnDown = false;
	static boolean btn2Down = false;

	private Vector3f position;

	public Player(Vector3f position) {
		this.position = position;
		handlers = new ArrayList<>();
	}

	public void teleportTo(Vector3f dest) {
		camera.setPosition(dest);
	}

	public void addHandler(ClickHandler h) {
		handlers.add(h);
	}

	/**
	 * fetches input events (like lookaround and walking) and updates the
	 * uniform values to also show this progress
	 *
	 * @param deltaTime
	 *            time passed since last frame
	 */

	public Vector3f getPosition() {
		return position;
	}

	public void update(float deltaTime) {

		// movement
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			// camera.processKeyboard(Camera.CameraMovement.FORAWRD, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			// camera.processKeyboard(Camera.CameraMovement.BACKWARD,
			// deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			// camera.processKeyboard(Camera.CameraMovement.LEFT, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			// camera.processKeyboard(Camera.CameraMovement.RIGHT, deltaTime);
		}
	}

	public static interface ClickHandler {

		public void onClickEvent(boolean down, int mouseButton);
	}
}
