package util;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static util.Util.lookAt;
import static util.Util.vCross;
import static util.Util.vMinus;
import static util.Util.vPlus;
import static util.Util.vScale;

import org.lwjgl.util.vector.Vector3f;
import static util.Util.vmMult;

/**
 *
 * @author Erik
 */
public class Camera {

	public enum CameraMovement {
		FORAWRD, BACKWARD, LEFT, RIGHT, UP, DOWN
	}

	private Vector3f position, front, right, up;
	private float yaw, pitch;
	private float actualYaw, actualPitch;
	private float movementSpeed, rotationsSpeed, zoom, sensitivity;

	public void restoreDefault() {
		position = new Vector3f(0, 0, 0);
		up = new Vector3f(0, 1, 0);
		yaw = (float) -Math.PI / 2;
		pitch = 0;
		actualYaw = yaw;
		actualPitch = pitch;
		front = new Vector3f(0, 0, -1);
		movementSpeed = 8f;
		rotationsSpeed = 10f;
		sensitivity = 0.003f;
		zoom = 45;
	}

	public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
		restoreDefault();
		this.position = position;
		this.yaw = yaw;
		this.pitch = pitch;
		this.actualPitch = pitch;
		this.actualYaw = yaw;
		updateCameraVectors();
	}

	public Matrix4f getViewMatrix() {
		return lookAt(position, vPlus(position, front), up);
	}

	public Matrix4f getSkyboxMatrix() {
		return lookAt(new Vector3f(0, 0, 0), front, up);
	}

	public Matrix4f getViewMatrix2() {
		Vector3f pos2 = new Vector3f(position);
		pos2 = vPlus(pos2, vScale(right, 0.1f));
		return lookAt(pos2, vPlus(pos2, front), up);
	}

	public void processKeyboard(CameraMovement direction, float deltaTime) {
		float velocity = movementSpeed * deltaTime;
		if (direction == CameraMovement.FORAWRD) {
			position = vPlus(position, vScale(vCross(up, right), velocity));
		} else if (direction == CameraMovement.BACKWARD) {
			position = vMinus(position, vScale(vCross(up, right), velocity));
		} else if (direction == CameraMovement.LEFT) {
			position = vMinus(position, vScale(right, velocity));
		} else if (direction == CameraMovement.RIGHT) {
			position = vPlus(position, vScale(right, velocity));
		} else if (direction == CameraMovement.UP) {
			position = vPlus(position, vScale(up, velocity));
		} else if (direction == CameraMovement.DOWN) {
			position = vMinus(position, vScale(up, velocity));
		}
	}

	public void processMouseMovement(float offsetX, float offsetY, float deltaTime) {
		yaw += offsetX * sensitivity;
		pitch += offsetY * sensitivity;

		if (pitch > 1.5f) {
			pitch = 1.5f;
		} else if (pitch < -1.5f) {
			pitch = -1.5f;
		}

		float deltaYaw;
		float deltaPitch;
		if (deltaTime * rotationsSpeed > 1) {
			deltaYaw = yaw - actualYaw;
			deltaPitch = pitch - actualPitch;
		} else {
			deltaYaw = (yaw - actualYaw) * deltaTime * rotationsSpeed;
			deltaPitch = (pitch - actualPitch) * deltaTime * rotationsSpeed;
		}

		actualYaw += deltaYaw;
		actualPitch += deltaPitch;

		updateCameraVectors();
	}

	public void processMouseScroll(float yoffset) {
		yoffset = (float) Math.pow(1.08, -yoffset);
		zoom *= yoffset;

		if (zoom <= 1.0f) {
			zoom = 1.0f;
		} else if (zoom >= 70.0f) {
			zoom = 70.0f;
		}
	}

	public float getFOV() {
		return zoom;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getDirection() {
		return front;
	}

	public void setUniformToPosition(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform), position.x, position.y, position.z);
	}

	public void setUniformToDirection(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform), front.x, front.y, front.z);
	}

	private void updateCameraVectors() {
		// front vector
		Vector3f front = new Vector3f(0, 0, 0);
		front.x = (float) (cos(actualYaw) * cos(actualPitch));
		front.y = (float) sin(actualPitch);
		front.z = (float) (sin(actualYaw) * cos(actualPitch));
		front = (Vector3f) front.normalise();
		this.front = front;

		// right vector
		Vector3f right = vCross(this.front, this.up);
		right = (Vector3f) right.normalise();
		this.right = right;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	/**
	 * roll the camera
	 *
	 * @param degrees
	 *            roll in degrees
	 */
	public void roll(float degrees) {
		Matrix4f rotUp = new Matrix4f();
		rotUp.rotate(degrees, front);
		up = vmMult(up, rotUp);
		right = vmMult(right, rotUp);
		updateCameraVectors();
	}

}
