package entities;

import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.Shader;
import util.Util;

public class Enemy extends WorldObject {

	private static final Mesh enemyMesh;
	private static final Mesh enemyPropMesh;
	private static final Material enemyMat;

	static {
		int dif = Util.loadTexture("EnemyTex.png");
		enemyMat = new Material(dif, 0);
		enemyMesh = new Mesh("Enemy.obj");
		enemyPropMesh = new Mesh("Enemy_propeler.obj");
	}
	
	private float propellerRot = 0f;

	public Enemy(Vector3f position) {
		super(new MeshInstance(enemyMesh, enemyMat), new MeshInstance(enemyPropMesh, enemyMat));
		float scale = 0.03f;
		super.model[0].setScale(new Vector3f(scale, scale, scale));
		super.model[1].setScale(new Vector3f(scale, scale, scale));
		this.position = position;
		this.position.normalise();
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
	}

	public void update(float deltaTime) {
		
		//set prop rot
		propellerRot += deltaTime * 10f;
		Matrix4f propRot = new Matrix4f();
		propRot.rotate(propellerRot, new Vector3f(0, 1, 0));
		modelMatrix[1] = propRot;
		
		
		
		int dx = -1, dy = 0;
		
		float viewDirAngleDelta = dy * deltaTime * 2f;

		Vector3f prePos = getPosition();

		Vector3f normPos = getPosition();
		normPos.normalise();
		
		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngleDelta, normPos);
		viewDir = Util.vmMult(viewDir, rot);
		viewDir.normalise();

		float speed = dx * deltaTime * 0.15f;
		walk(speed, prePos);
	}
}
