/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 *
 * @author Erik
 */
public class Mesh {

	public static int stride = 8 * 4;

	private float[] data;
	private int[] indices;
	private boolean EBOMode;

	private int VAO;

	public Mesh(String filename) {
		this(ObjectLoader.loadObject(filename));
	}

	public Mesh(float[] allData) {
		this.data = allData;
		EBOMode = false;
		generateVAO();
	}

	public Mesh(float[] data, int[] indices) {
		this.data = data;
		this.indices = indices;
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
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * 4);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6 * 4);
		glEnableVertexAttribArray(2);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public int getVertCount() {
<<<<<<< HEAD
		if (EBOMode) {
			return indices.length;
		} else {
			return data.length;
		}
=======
		return EBOMode ? indices.length : data.length;
>>>>>>> origin/master
	}

	private void generateVAO() {
		VAO = glGenVertexArrays();
		glBindVertexArray(VAO);

		int VBO;
		VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		FloatBuffer vertexB = BufferUtils.createFloatBuffer(data.length).put(data);
		vertexB.flip();
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
			IntBuffer indexB = BufferUtils.createIntBuffer(indices.length);
			indexB.put(indices);
			indexB.flip();
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexB, GL_STATIC_DRAW);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		}

		glBindVertexArray(0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public int getVAO() {
		return VAO;
	}
}
