package entities;

import java.util.ArrayList;
import java.util.Random;

import light.LightHandler;
import light.SpotLight;

import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.ObjectLoader;
import util.Shader;
import util.Util;

public class Enemy extends WorldObject {

	private static final Mesh enemyMesh;
	private static final Mesh enemyPropMesh;
	private static final Material enemyMat;
	private static Random ran;

	static {
		int dif = Util.loadTexture("EnemyTex.png");
		enemyMat = new Material(dif, 0);
		enemyMesh = ObjectLoader.loadObjectEBO("Enemy.obj");
		enemyPropMesh = ObjectLoader.loadObjectEBO("Enemy_propeler.obj");
		ran = new Random();
	}

	private float propellerRot = 0f;
	private float timePassed;
	private Player player;
	private Vector3f destinationDir;
	private LightHandler lh;
	private SpotLight light;

	public Enemy(Vector3f position, Player player, LightHandler lh, ArrayList<Shader> shaders) {
		super(new MeshInstance(enemyMesh, enemyMat), new MeshInstance(enemyPropMesh, enemyMat));
		this.player = player;
		this.lh = lh;
		float scale = 0.03f;
		super.model[0].setScale(new Vector3f(scale, scale, scale));
		super.model[1].setScale(new Vector3f(scale, scale, scale));
		this.position = position;
		this.position.normalise();
		viewDir = new Vector3f(1.1f, 0.1f, 0.1f);
		light = new SpotLight(new Vector3f(1.0f, 1.0f, 1.0f), position, viewDir, 20, 34, 10);
		lh.addLight(light, shaders);

		walk(0.01f, new Vector3f(0.1f, 0.1f, 0.1f));
		update(0.001f);
	}

	public void update(float deltaTime) {
		timePassed += deltaTime;

		Vector3f lightPos = new Vector3f(position);
		lightPos.scale(1.2f);
		light.setPosition(lightPos);
		Vector3f lightDir = new Vector3f(-viewDir.x, -viewDir.y, -viewDir.z);
		Vector3f right = Vector3f.cross(lightDir, position, null);
		right.normalise();
		Matrix4f rot = new Matrix4f();
		rot.rotate(0.5f, right);
		lightDir = Util.vmMult(lightDir, rot);
		light.setDirection(lightDir);
		lh.updateLight(light);

		// set prop rot
		propellerRot += deltaTime * 10f;
		Matrix4f propRot = new Matrix4f();
		propRot.rotate(propellerRot, new Vector3f(0, 1, 0));
		modelMatrix[1] = propRot;

		// AI
		float dx = -1, dy = 0;

		Vector3f prePos = getPosition();

		Vector3f normPos = getPosition();
		normPos.normalise();

		destinationDir = Vector3f.sub(position, player.getPosition(), null);
		destinationDir.normalise();

		Vector3f deltaDir = Vector3f.sub(destinationDir, viewDir, null);
		deltaDir.scale(deltaTime * 2);
		viewDir = Vector3f.add(viewDir, deltaDir, null);

		viewDir.normalise();

		float speed = dx * deltaTime * 0.05f;
		walk(speed, prePos);
	}
}
