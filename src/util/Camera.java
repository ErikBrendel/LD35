package util;

import static org.lwjgl.opengl.GL20.glUniform3f;
import static util.Util.lookAt;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Erik
 */
public class Camera {

	public enum CameraMovement {
		FORAWRD, BACKWARD, LEFT, RIGHT, UP, DOWN
	}

	private Vector3f position, up, lookAt, prePos;
	private float zoom;
	private float cameraDistance;
	private Matrix4f projection;

	public void restoreDefault() {
		position = new Vector3f(0, 0, 0);
		up = new Vector3f(0, 1, 0);
		lookAt = new Vector3f(0, 0, 0);
		zoom = 45;
		cameraDistance = 2;
	}

	public Camera(Vector3f origin, Vector3f playerPos, Vector3f enemyPos, float cameraDistance) {
		restoreDefault();
		setWorldView(origin, playerPos, enemyPos);
		this.cameraDistance = cameraDistance;
		projection = Util.perspective(zoom, (float) Display.getWidth() / (float) Display.getHeight(), 0.1f, 100f);
	}

	public void setWorldView(Vector3f origin, Vector3f playerPos, Vector3f enemyPos) {
		prePos = new Vector3f(position);
		Vector3f dif = new Vector3f();
		dif = Vector3f.sub(enemyPos, playerPos, dif);
		dif.scale(0.3f);
		Vector3f midpoint = new Vector3f();
		midpoint = Vector3f.add(playerPos, dif, midpoint);
		Vector3f direction = new Vector3f();
		direction = Vector3f.sub(midpoint, origin, direction);
		direction.normalise();
		direction.scale(cameraDistance);
		position = Vector3f.add(origin, direction, position);
		Vector3f playerDir = new Vector3f();
		playerDir = Vector3f.sub(playerPos, enemyPos, playerDir);
		up = Vector3f.cross(direction, playerDir, up);
		up.normalise();
		cameraDistance = playerDir.length() / 1.2f + 1.4f;
		if (cameraDistance < 2.3) {
			lookAt = Vector3f.cross(dif, direction, null);
			lookAt.normalise();
			lookAt.scale(2.3f - cameraDistance);
			Vector3f posOffset = new Vector3f(lookAt);
			posOffset.scale(0.6f);
			Vector3f.sub(position, posOffset, position);
		} else {
			lookAt = new Vector3f();
		}
	}

	public Vector3f getUp() {
		return up;
	}

	public Vector3f getLookAt() {
		return lookAt;
	}

	public Matrix4f getProjectionMatrix() {
		return projection;
	}

	public Matrix4f getViewMatrix() {
		return lookAt(position, lookAt, up);
	}

	public Matrix4f getSkyboxMatrix() {
		return lookAt(new Vector3f(0, 0, 0), position, up);
	}

	public float getFOV() {
		return zoom;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void apply(Shader shader) {
		glUniform3f(shader.getUniform("viewPos"), position.getX(), position.getY(), position.getZ());
	}

	public Vector3f getVelocity() {
		return Vector3f.sub(position, prePos, null);
	}

}
