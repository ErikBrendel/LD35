package particles;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Matrix4f;
import util.Mesh;

import com.sun.prism.impl.BufferUtil;

public class ParticleHandler {
	private int amount;
	private Vector3f origin;
	private Random random;
	private Particle parent;

	private int VAO;

	private HashSet<Particle> particles;

	private Matrix4f[] matrices;
	private FloatBuffer data;

	public ParticleHandler(Vector3f origin, Particle parent, Mesh mesh, Material material) {
		random = new Random();
		this.origin = origin;

		VAO = mesh.getVAO();
		glBindVertexArray(VAO);
		int buffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glEnableVertexAttribArray(3);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 64, 0);
		glEnableVertexAttribArray(4);
		glVertexAttribPointer(4, 4, GL_FLOAT, false, 64, 16);
		glEnableVertexAttribArray(5);
		glVertexAttribPointer(5, 4, GL_FLOAT, false, 64, 32);
		glEnableVertexAttribArray(6);
		glVertexAttribPointer(6, 4, GL_FLOAT, false, 64, 48);

		glVertexAttribDivisor(3, 1);
		glVertexAttribDivisor(4, 1);
		glVertexAttribDivisor(5, 1);
		glVertexAttribDivisor(6, 1);

		glBindVertexArray(0);
	}

	public void emit(int amount) {
		this.amount += amount;
		for (int i = 0; i < amount; i++) {
			particles.add(parent.getInstance());
		}
		matrices = new Matrix4f[this.amount];
	}

	public void update(float deltaTime) {
		int o = 0;
		for (Particle p : particles) {
			matrices[o] = p.generateModel(random);
			o++;
		}
		data = BufferUtil.newFloatBuffer(amount * 16);

		for (int i = 0; i < amount; i++) {
			data.put(matrices[i].getDataArray());
		}

		data.flip();
	}

	public void render() {

	}
}
