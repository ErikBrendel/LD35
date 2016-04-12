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

		float[] data = { -0.5f, -0.5f, -0.5f, 0.25f, 2.0f / 3.0f, 0.5f, -0.5f, -0.5f, 0.50f, 2.0f / 3.0f, 0.5f, 0.5f, -0.5f, 0.50f, 1.0f / 3.0f, 0.5f, 0.5f, -0.5f, 0.50f, 1.0f / 3.0f, -0.5f, 0.5f, -0.5f, 0.25f, 1.0f / 3.0f, -0.5f, -0.5f, -0.5f, 0.25f, 2.0f / 3.0f,

		-0.5f, -0.5f, 0.5f, 1.00f, 2.0f / 3.0f, 0.5f, -0.5f, 0.5f, 0.75f, 2.0f / 3.0f, 0.5f, 0.5f, 0.5f, 0.75f, 1.0f / 3.0f, 0.5f, 0.5f, 0.5f, 0.75f, 1.0f / 3.0f, -0.5f, 0.5f, 0.5f, 1.00f, 1.0f / 3.0f, -0.5f, -0.5f, 0.5f, 1.00f, 2.0f / 3.0f,

		-0.5f, 0.5f, 0.5f, 0.00f, 1.0f / 3.0f, -0.5f, 0.5f, -0.5f, 0.25f, 1.0f / 3.0f, -0.5f, -0.5f, -0.5f, 0.25f, 2.0f / 3.0f, -0.5f, -0.5f, -0.5f, 0.25f, 2.0f / 3.0f, -0.5f, -0.5f, 0.5f, 0.00f, 2.0f / 3.0f, -0.5f, 0.5f, 0.5f, 0.00f, 1.0f / 3.0f,

		0.5f, 0.5f, 0.5f, 0.75f, 1.0f / 3.0f, 0.5f, 0.5f, -0.5f, 0.50f, 1.0f / 3.0f, 0.5f, -0.5f, -0.5f, 0.50f, 2.0f / 3.0f, 0.5f, -0.5f, -0.5f, 0.50f, 2.0f / 3.0f, 0.5f, -0.5f, 0.5f, 0.75f, 2.0f / 3.0f, 0.5f, 0.5f, 0.5f, 0.75f, 1.0f / 3.0f,

		-0.5f, -0.5f, -0.5f, 0.25f, 2.0f / 3.0f, 0.5f, -0.5f, -0.5f, 0.50f, 2.0f / 3.0f, 0.5f, -0.5f, 0.5f, 0.50f, 0.999f, 0.5f, -0.5f, 0.5f, 0.50f, 0.999f, -0.5f, -0.5f, 0.5f, 0.25f, 0.999f, -0.5f, -0.5f, -0.5f, 0.25f, 2.0f / 3.0f,

		-0.5f, 0.5f, -0.5f, 0.25f, 1.0f / 3.0f, 0.5f, 0.5f, -0.5f, 0.50f, 1.0f / 3.0f, 0.5f, 0.5f, 0.5f, 0.50f, 0.001f, 0.5f, 0.5f, 0.5f, 0.50f, 0.001f, -0.5f, 0.5f, 0.5f, 0.25f, 0.001f, -0.5f, 0.5f, -0.5f, 0.25f, 1.0f / 3.0f };
		int[] indices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };
		int[] vertexDataSizes = { 3, 2 };
		mesh = new Mesh(data, indices, vertexDataSizes);
	}

	public Skybox(String texturePath) {
		texture = Util.loadTexture(texturePath);
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
