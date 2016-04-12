package util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Skybox {
	private int texture;
	private static Mesh mesh;
	private static Shader shader;

	static {
		shader = Shader.fromFile("Skybox.vert", "Skybox.frag");
		shader.use();

		float[] data = {
				// front
				-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
				// back
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,

		};

		int[] indices = { 0, 2, 3,// f1
				// front
				0, 1, 2, 2, 3, 0,
				// top
				1, 5, 6, 6, 2, 1,
				// back
				7, 6, 5, 5, 4, 7,
				// bottom
				4, 0, 3, 3, 7, 4,
				// left
				4, 5, 1, 1, 0, 4,
				// right
				3, 2, 6, 6, 7, 3, };

		int[] vertexDataSizes = { 3 };
		mesh = new Mesh(data, indices, vertexDataSizes);
	}

	public Skybox(String texturePath) {
		texture = Util.loadCubeMap(texturePath);
	}

	public int getTexture() {
		return texture;
	}

	public void render(Camera camera) {
		shader.use();

		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		Matrix4f projection = Util.perspective(camera.getFOV(), windowSize.width / (double) windowSize.height, 0.1, 100);
		glUniformMatrix4(shader.getUniform("projection"), false, projection.getData());

		Matrix4f view = camera.getSkyboxMatrix();
		glUniformMatrix4(shader.getUniform("view"), false, view.getData());

		glDepthMask(false);
		glUniform1i(shader.getUniform("skybox"), texture);
		mesh.render();
		glDepthMask(true);
	}
}
