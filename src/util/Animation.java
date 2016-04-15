/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
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

	public Animation() {
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
	public void addKeyframe(Keyframe f) {
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

	public void addVertex(Vertex v) {
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

	public int getVAO() {
		if (invalidVAO) {
			//re-generate VAO

			int stride = (8 + getBoneCount()) * SIZEOF_FLOAT;

			glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * SIZEOF_FLOAT);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * SIZEOF_FLOAT);
			glEnableVertexAttribArray(2);

		}
		return VAO;
	}

	public int getKeyFrameCount() {
		return keyframes.size();
	}

	public int getBoneCount() {
		return keyframes.isEmpty() ? 0 : keyframes.get(0).getBones().size();
	}

	@Override
	public String toString() {
		return "Animation(" + indizes.size() + " vertizes, " + vertices.size() + " unique ones, " + getKeyFrameCount() + " keyframes for " + getBoneCount() + "bones)";
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

		public Vector2f getUv() {
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
