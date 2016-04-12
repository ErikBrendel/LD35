/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 *
 * @author Erik
 */
public class Mesh {

	public int stride;

	private float[] data;
	private int[] indices;
	private int[] vertexDataSizes;
	private boolean EBOMode;

	private int VAO;

	public Mesh(String filename) {
		this(ObjectLoader.loadObject(filename));
	}

	public Mesh(float[] allData) {
		this.data = allData;
		vertexDataSizes = new int[3];
		vertexDataSizes[0] = 3;
		vertexDataSizes[1] = 3;
		vertexDataSizes[2] = 2;
		this.stride = 0;
		for (int i : vertexDataSizes) {
			this.stride += 4 * i;
		}
		EBOMode = false;
		generateVAO();
	}

	public Mesh(float[] data, int[] indices, int[] vertexDataSizes) {
		this.data = data;
		this.indices = indices;
		this.vertexDataSizes = vertexDataSizes;
		this.stride = 0;
		for (int i : vertexDataSizes) {
			this.stride += 4 * i;
		}
		EBOMode = true;
		generateVAO();
	}

	/**
	 * this method loads the objects vertex data into a given VBO. Please
	 * generate the vbo before. The data consist of vec3 position, vec3 normal,
	 * vec2 texCoord
	 *
	 * This function uses GL_ARRAY_BUFFER, so make sure no other VBO is bound to
	 * this currently
	 *
	 * @param VBO
	 *            the VBO
	 */
	public void loadToBuffer(int VBO) {
		FloatBuffer vertexB = BufferUtils.createFloatBuffer(data.length).put(data);
		vertexB.flip();

		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, vertexB, GL_STATIC_DRAW);
		int offset = 0;
		for (int i = 0; i < vertexDataSizes.length; i++) {
			glVertexAttribPointer(i, vertexDataSizes[i], GL_FLOAT, false, stride, offset);
			glEnableVertexAttribArray(i);
			offset += vertexDataSizes[i] * 4;
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public int getVertCount() {
		return EBOMode ? indices.length : data.length;
	}

	private void generateVAO() {
		VAO = glGenVertexArrays();
		glBindVertexArray(VAO);

		int VBO;
		VBO = glGenBuffers();
		FloatBuffer vertexB = BufferUtils.createFloatBuffer(data.length).put(data);
		vertexB.flip();
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, vertexB, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * 4);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6 * 4);
		glEnableVertexAttribArray(2);

		if (EBOMode) {
			int EBO = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);

			IntBuffer indexB = BufferUtils.createIntBuffer(indices.length).put(indices);
			indexB.flip();
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexB, GL_STATIC_DRAW);
		}

		glBindVertexArray(0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public int getVAO() {
		return VAO;
	}

	public void render() {
		glBindVertexArray(VAO);
		if (EBOMode) {
			glDrawElements(GL_TRIANGLES, getVertCount(), GL_UNSIGNED_INT, 0);
		} else {
			glDrawArrays(GL_TRIANGLES, 0, getVertCount());
		}
		glBindVertexArray(0);
	}
}
