/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * A mesh with animated bones
 *
 * @author Erik
 */
public class Animation {

	public static final int SIZEOF_FLOAT = 4;

	private SortedArrayList<Keyframe> keyframes;
	private ArrayList<Vertex> vertices;
	private ArrayList<Integer> indizes;
	private boolean invalidVAO;
	private int VAO;
	private Shader shader;

	Animation() {
		keyframes = new SortedArrayList<>();
		vertices = new ArrayList<>();
		indizes = new ArrayList<>();
		invalidVAO = true;
	}

	/**
	 * add a keyframe for a bone to this animation. If there is already a
	 * keyframe at this timestamp, they will be merged to a certain degree.
	 *
	 * @param f a new keyframe for this animation
	 */
	void addKeyframe(Keyframe f) {
		invalidVAO = true;
		//perform keyframe merging if necessary
		Keyframe sameTime = null;
		for (int i = 0; i < keyframes.size(); i++) {
			if (keyframes.get(i).getTimestamp().equals(f.getTimestamp())) {
				sameTime = keyframes.get(i);
				break;
			}
		}
		if (sameTime == null) {
			//no keyframe for this timestamp, just dd the new one
			keyframes.insertSorted(f);
		} else {
			//there is already a keyframe at this time, merge them
			for (Entry<Integer, Matrix4f> boneState : f.getBones().entrySet()) {
				Matrix4f prev = sameTime.getBones().put(boneState.getKey(), boneState.getValue());
				if (prev != null) {
					System.err.println("WARNING: Keyframe merging of same bone (overriding animation data). (Timestamp: " + f.getTimestamp() + ")");
				}
			}
		}

	}

	void addVertex(Vertex v) {
		//check if an equal vertex i alreay present
		int index = -1;
		for (int i = 0; i < vertices.size(); i++) {
			if (vertices.get(i).equals(v)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			//insert newly vertex
			vertices.add(v);
			indizes.add(vertices.size() - 1);
		} else {
			//only add the index
			indizes.add(index);
		}
		invalidVAO = true;
	}

	private int getVAO() {
		if (invalidVAO) {
			//
			//
			//
			//re-generate VAO
			int boneCount = getBoneCount();
			int vertexFloatCount = 8 + boneCount;
			int stride = vertexFloatCount * SIZEOF_FLOAT;

			//create VAO
			VAO = glGenVertexArrays();
			glBindVertexArray(VAO);

			//create the big buffer array
			float[] buffer = new float[vertexFloatCount * vertices.size()];
			for (int v = 0; v < vertices.size(); v++) {
				int bp = v * vertexFloatCount; //bufferPointer
				Vertex vert = vertices.get(v);
				buffer[bp++] = vert.getPosition().x;
				buffer[bp++] = vert.getPosition().y;
				buffer[bp++] = vert.getPosition().z;
				buffer[bp++] = vert.getNormal().x;
				buffer[bp++] = vert.getNormal().y;
				buffer[bp++] = vert.getNormal().z;
				buffer[bp++] = vert.getUV().x;
				buffer[bp++] = vert.getUV().y;
				for (int b = 0; b < boneCount; b++) {
					buffer[bp++] = vert.getBoneWeights().get(b);
				}
			}
			
			//for buffer data debugging
			/*for (int i = 0; i < buffer.length; i++) {
				if (i % vertexFloatCount == 0) {
					System.err.println();
				}
				System.err.print(buffer[i] + "  ");
				if (i % vertexFloatCount == 7) {
					System.err.print("     ");
				}
			}/**/
			//paste float[] array into FloatBuffer
			int VBO;
			VBO = glGenBuffers();
			FloatBuffer vertexB = BufferUtils.createFloatBuffer(buffer.length).put(buffer);
			vertexB.flip();
			glBindBuffer(GL_ARRAY_BUFFER, VBO);
			glBufferData(GL_ARRAY_BUFFER, vertexB, GL_STATIC_DRAW);

			//set the reading paradigmas for the vertex shader
			glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * SIZEOF_FLOAT);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * SIZEOF_FLOAT);
			glEnableVertexAttribArray(2);
			glVertexAttribPointer(3, boneCount, GL_FLOAT, false, stride, 8 * SIZEOF_FLOAT);
			glEnableVertexAttribArray(3);

			//create EBO
			int EBO = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);

			//paste EBO data into IntBuffer
			int[] indizesArray = new int[indizes.size()];
			for (int i = 0; i < indizes.size(); i++) {
				indizesArray[i] = indizes.get(i);
			}
			IntBuffer indexB = BufferUtils.createIntBuffer(indizesArray.length).put(indizesArray);
			indexB.flip();
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexB, GL_STATIC_DRAW);

			//finished, clean up
			glBindVertexArray(0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

			//
			//
			//
			//
		}
		return VAO;
	}

	/**
	 * return the Shader to draw this object. Be sure to call generateShader
	 * first.
	 *
	 * @return the Shader to draw this object
	 */
	public Shader getShader() {
		return shader;
	}

	/**
	 * Generate a shader-Object for this Animation. Each animation object needs
	 * an own (dynamically generated) Vertex shader, and this method thakes care
	 * of this.
	 * <br><br>
	 * You can pass optional parameters for your fragment shader to this
	 * function, or leave the second argument at null.
	 * <br><br>
	 * This method calls Shader.use() on its own shader to be able to set its
	 * uniforms.
	 *
	 *
	 * @param frag the name of the fragment shader to use
	 * @param params optional parameters
	 */
	public void generateShader(String frag, HashMap<String, Object> params) {
		params = (params == null) ? new HashMap<>() : params;
		
		//make sure the VAO is instantiated
		getVAO();

		//create the shader object
		params.put("BONE_COUNT", getBoneCount());
		params.put("KEY_FRAME_COUNT", getKeyFrameCount());
		shader = Shader.fromFile("Animation.vert", frag, params);
		shader.use();

		//set uniform data
		int bonecount = getBoneCount();
		for (int k = 0; k < keyframes.size(); k++) {
			Keyframe frame = keyframes.get(k);
			glUniform1f(shader.getUniform("key_frame_time[" + k + "]"), frame.getTimestamp());
			for (int b = 0; b < bonecount; b++) {
				String arr = "[" + (k * bonecount + b) + "]";
				boolean forThisBone = frame.getBones().get(b) != null;
				glUniform1i(shader.getUniform("forThisBone" + arr), forThisBone ? 1 : 0);

				glUniformMatrix4(shader.getUniform("bones" + arr), false, frame.getBones().get(b).getData());
			}
		}
	}

	/**
	 * Renders the object with its shader
	 */
	public void render() {
		shader.use();

		glBindVertexArray(VAO);
		glDrawElements(GL_TRIANGLES, vertices.size(), GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}

	private int getKeyFrameCount() {
		return keyframes.size();
	}

	private int getBoneCount() {
		int count = 0;
		for (int k = 0; k < keyframes.size(); k++) {
			count = Math.max(count, keyframes.get(k).getBones().size());
		}
		return count;
	}

	@Override
	public String toString() {
		return "Animation(" + indizes.size() + " vertizes, " + vertices.size() + " unique ones, " + getKeyFrameCount() + " keyframes for " + getBoneCount() + " bones)";
	}

	/**
	 * A keyframe for an animation, containing its timestamp and all bone
	 * transformations
	 */
	public static class Keyframe implements Comparable<Keyframe> {

		private Float timestamp;
		private HashMap<Integer, Matrix4f> bones; //from bone-id to transformation matrix

		public Keyframe() {
			bones = new HashMap<>();
			timestamp = 0f;
		}

		public Keyframe(Float timestamp) {
			this.timestamp = timestamp;
			this.bones = new HashMap<>();
		}

		@Override
		public int compareTo(Keyframe o) {
			return timestamp.compareTo(o.timestamp);
		}

		public HashMap<Integer, Matrix4f> getBones() {
			return bones;
		}

		public Float getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Float timestamp) {
			this.timestamp = timestamp;
		}

	}

	public static class Vertex {

		private Vector3f position;
		private Vector3f normal;
		private Vector2f uv;
		private ArrayList<Float> boneWeights;

		public Vertex(Vector3f position, Vector3f normal, Vector2f uv) {
			this.position = position;
			this.normal = normal;
			this.uv = uv;
			boneWeights = new ArrayList<>();
		}

		public Vertex() {
			boneWeights = new ArrayList<>();
		}

		public void setNormal(Vector3f normal) {
			this.normal = normal;
		}

		public void setPosition(Vector3f position) {
			this.position = position;
		}

		public void setUv(Vector2f uv) {
			this.uv = uv;
		}

		public Vector3f getPosition() {
			return position;
		}

		public Vector3f getNormal() {
			return normal;
		}

		public Vector2f getUV() {
			return uv;
		}

		public ArrayList<Float> getBoneWeights() {
			return boneWeights;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Vertex)) {
				return false;
			}
			Vertex v2 = (Vertex) o;

			//enough comparison...
			//there wont be any vertices at same position, normal and uv with different bone weights, i hope
			return position.equals(v2.position) && normal.equals(v2.normal) && uv.equals(v2.uv);

		}

	}

}
