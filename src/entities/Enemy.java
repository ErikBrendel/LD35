package entities;

import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Mesh;
import util.MeshInstance;
import util.Shader;
import util.Util;

public class Enemy {
	private static Mesh enemyMesh;
	private static Material enemyMat;

	static {
		int dif = Util.loadTexture("EnemyTex.png");
		// int spec = Util.loadTexture("container2_specular.png");
		enemyMat = new Material(dif, 0);
		enemyMesh = new Mesh("Enemy.obj");
	}

	private Vector3f position;
	private MeshInstance model;

	public Enemy(Vector3f position) {
		this.position = position;
		model = new MeshInstance(enemyMesh, enemyMat);
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void update(float deltaTime) {

	}

	public void render(Shader shader) {
		model.setLocation(position);
		model.setScale(new Vector3f(0.1f, 0.1f, 0.1f));
		// model.setRotation(position);
		model.render(shader);
	}
}
