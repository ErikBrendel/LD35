package entities;

import org.lwjgl.util.vector.Vector3f;

public class Enemy {
	private Vector3f position;

	public Enemy(Vector3f position) {
		this.position = position;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void update(float deltaTime) {

	}

	public void render() {

	}
}
