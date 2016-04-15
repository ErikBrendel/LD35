/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL20.glUniform3f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Erik
 */
public class Player {

	Camera camera;
	private ArrayList<ClickHandler> handlers;
	static boolean btnDown = false;
	static boolean btn2Down = false;

	public Player() {
		camera = new Camera(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), -90, 0);
		handlers = new ArrayList<>();
	}

	public void teleportTo(Vector3f dest) {
		camera.setPosition(dest);
	}

	public Camera getCamera() {
		return camera;
	}

	public void addHandler(ClickHandler h) {
		handlers.add(h);
	}

	public Matrix4f getViewMatrix() {
		return camera.getViewMatrix();
	}

	public Matrix4f getProjectionMatrix() {
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		return Util.perspective(getCamera().getFOV(), windowSize.width / (double) windowSize.height, 0.01, 1000);
	}

	/**
	 * set shaders uniform variables to this players data
	 *
	 * @param shader
	 */
	public void applyToShader(Shader shader, boolean setViewPos) {
		if (setViewPos) {
			glUniform3f(shader.getUniform("viewPos"), getCamera().getPosition().x, getCamera().getPosition().y, getCamera().getPosition().z);
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
		// lClick
		if (Mouse.isButtonDown(0)) {
			if (!btnDown) {
				btnDown = true;
				handlers.forEach((ClickHandler h) -> h.onClickEvent(true, 1));
			}
		} else {
			if (btnDown) {
				btnDown = false;
				handlers.forEach((ClickHandler h) -> h.onClickEvent(false, 1));
			}
		}
		if (Mouse.isButtonDown(1)) {
			if (!btn2Down) {
				btn2Down = true;
				handlers.forEach((ClickHandler h) -> h.onClickEvent(true, 2));
			}
		} else {
			if (!btn2Down) {
				btn2Down = false;
				handlers.forEach((ClickHandler h) -> h.onClickEvent(false, 2));
			}
		}

		// lookaround
		camera.processMouseMovement(Mouse.getDX(), Mouse.getDY(), deltaTime);

		// movement
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			camera.processKeyboard(Camera.CameraMovement.FORAWRD, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			camera.processKeyboard(Camera.CameraMovement.BACKWARD, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			camera.processKeyboard(Camera.CameraMovement.LEFT, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			camera.processKeyboard(Camera.CameraMovement.RIGHT, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			camera.processKeyboard(Camera.CameraMovement.UP, deltaTime);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			camera.processKeyboard(Camera.CameraMovement.DOWN, deltaTime);
		}
	}

	public static interface ClickHandler {

		public void onClickEvent(boolean down, int mouseButton);
	}
}
