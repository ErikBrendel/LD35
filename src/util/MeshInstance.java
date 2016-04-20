/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import org.lwjgl.util.vector.Vector3f;

/**
 * the "implementation" of a Mesh, containing a mesh, material and model matrix
 *
 * @author Erik
 */
public class MeshInstance {

	private Mesh mesh;
	private Material material;
	private Vector3f location, rotation, scale;

	public MeshInstance(Mesh mesh, Material material) {
		this(mesh, material, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
	}

	public MeshInstance(Mesh mesh, Material material, Vector3f location, Vector3f rotation, Vector3f scale) {
		this.mesh = mesh;
		this.material = material;
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
	}

	public void setLocation(Vector3f location) {
		this.location = location;
	}

	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	public Vector3f getLocation() {
		return location;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public Vector3f getScale() {
		return scale;
	}

	public void render(Shader shader) {
		shader.use();
		// material.apply(shader);

		Matrix4f model = new Matrix4f();
		model.translate(location);
		model.rotate(rotation.x, new Vector3f(1, 0, 0));
		model.rotate(rotation.y, new Vector3f(0, 1, 0));
		model.rotate(rotation.z, new Vector3f(0, 0, 1));
		model.scale(scale);

		glUniformMatrix4(shader.getUniform("model"), false, model.getData());

		mesh.render();
	}

}
