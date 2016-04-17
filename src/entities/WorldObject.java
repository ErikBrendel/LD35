package entities;

import org.lwjgl.util.vector.Vector3f;
import util.Matrix4f;
import util.MeshInstance;
import util.Shader;

/**
 * an object on a planet
 *
 * @author Erik
 */
public abstract class WorldObject {

	private static final Vector3f WORLD_FRONT = new Vector3f(1, 0, 0);
	private static final Vector3f WORLD_NORTH = new Vector3f(0, 1, 0);

	protected Vector3f position;
	protected Vector3f viewDir;
	protected Vector3f prePos;
	protected MeshInstance[] model;
	protected Matrix4f[] modelMatrix;

	public WorldObject(MeshInstance... model) {
		prePos = new Vector3f();
		position = new Vector3f();
		this.model = model;
		modelMatrix = new Matrix4f[model.length];
		for (int i = 0; i < modelMatrix.length; i++) {
			modelMatrix[i] = new Matrix4f();
		}
	}

	/**
	 * returns the position data object-safe (so you can modify it all you want
	 * without affecting this model)
	 *
	 * @return this models Position in 3D space
	 */
	public Vector3f getPosition() {
		return new Vector3f(position);
	}

	public Vector3f getVelocity() {
		return Vector3f.sub(position, prePos, null);
	}

	public Vector3f getViewDir() {
		return viewDir;
	}

	public void render(Shader shader) {
		prePos = new Vector3f(position);
		for (int m = 0; m < model.length; m++) {
			model[m].setLocation(position);
			Vector3f rotationAxis = new Vector3f();
			rotationAxis = Vector3f.cross(WORLD_NORTH, position, rotationAxis);
			rotationAxis.normalise();

			Vector3f normPos = new Vector3f(position);
			normPos.normalise();

			float angle = (float) Math.acos(Vector3f.dot(normPos, WORLD_NORTH));

			Vector3f plainPos = new Vector3f(position.x, 0, position.z);
			plainPos.normalise();

			float baseRotationAngle = (float) Math.PI - (float) Math.acos(Vector3f.dot(plainPos, WORLD_FRONT));

			Vector3f aequatorparallel = new Vector3f();
			aequatorparallel = Vector3f.cross(normPos, WORLD_NORTH, aequatorparallel);
			aequatorparallel.normalise();

			Vector3f nordDirection = new Vector3f();
			nordDirection = Vector3f.cross(normPos, aequatorparallel, nordDirection);
			nordDirection.normalise();

			float viewDirAngle = (float) Math.acos(Vector3f.dot(viewDir, nordDirection));

			Vector3f minustestvektor = Vector3f.cross(nordDirection, viewDir, null);
			minustestvektor.normalise();
			minustestvektor.scale(0.2f);
			Vector3f testPoint = Vector3f.add(normPos, minustestvektor, null);

			if (testPoint.length() < 1.0) {
				viewDirAngle *= -1;
			}

			if (Vector3f.cross(WORLD_FRONT, plainPos, null).y > 0) {
				baseRotationAngle *= -1;
			}
			org.lwjgl.util.vector.Matrix4f rot = new org.lwjgl.util.vector.Matrix4f();
			rot.rotate(viewDirAngle, normPos);
			rot.rotate(angle, rotationAxis);
			rot.rotate(baseRotationAngle, WORLD_NORTH);

			rot = Matrix4f.mul(rot, modelMatrix[m], rot);

			model[m].setRotationMatrix(rot);
			// model[m].setScale(new Vector3f(0.07f, 0.07f, 0.07f));
			model[m].render(shader);
		}
	}

	protected void walk(float speed, Vector3f prePos) {
		if (speed != 0) {
			Vector3f walkDir = new Vector3f(viewDir);
			walkDir.normalise();
			walkDir.scale(speed);
			position = Vector3f.add(position, walkDir, position);
			position.normalise();
			Vector3f positionDelta = new Vector3f();
			if (speed < 0) {
				positionDelta = Vector3f.sub(position, prePos, positionDelta);
			} else {
				positionDelta = Vector3f.sub(prePos, position, positionDelta);
			}
			Vector3f right = new Vector3f();
			right = Vector3f.cross(positionDelta, position, right);
			viewDir = Vector3f.cross(right, position, viewDir);
			viewDir.normalise();
		}
	}
}
