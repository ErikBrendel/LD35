package particles;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

import org.lwjgl.util.vector.Vector3f;

import util.Camera;
import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.Shader;

import com.sun.prism.impl.BufferUtil;

public class ParticleHandler {
	private int vertexCount;
	private int amount;
	private Vector3f origin;
	private Random random;
	private Particle parent;

	private Material material;

	private HashSet<Particle> particles;

	private Matrix4f[] matrices;
	private Shader shader;

	private Mesh mesh;

	private int VAO;

	public ParticleHandler(Vector3f origin, Particle parent, Mesh mesh, Material material, Shader shader) {
		random = new Random();

		particles = new HashSet<>();

		this.origin = origin;

		this.mesh = mesh;

		this.parent = parent;

		this.vertexCount = mesh.getVertCount();

		this.material = material;
		this.shader = shader;
		matrices = new Matrix4f[0];
		VAO = mesh.getVAO();
		FloatBuffer data = BufferUtil.newFloatBuffer(amount * 16);
		for (int i = 0; i < amount; i++) {
			data.put(matrices[i].getDataArray());
		}
		data.flip();
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

	public void setOrigin(Vector3f origin) {
		this.origin = origin;
	}

	public void emit(int amount) {
		this.amount += amount;
		for (int i = 0; i < amount; i++) {
			Particle p = parent.getInstance();
			p.generateStartValues(random, origin);
			particles.add(p);
		}
		matrices = new Matrix4f[this.amount];
	}

	public void update(float deltaTime) {
		int o = 0;
		for (Particle p : particles) {
			matrices[o] = p.generateModel(random, deltaTime);
			o++;
			if (p.isDead()) {
				o--;
			}
		}
		particles = (HashSet<Particle>) particles.stream().filter((Particle part) -> !part.isDead()).collect(Collectors.toSet());

		amount = particles.size();

		FloatBuffer data = BufferUtil.newFloatBuffer(amount * 16);
		for (int i = 0; i < amount; i++) {
			data.put(matrices[i].getDataArray());
		}

		data.flip();
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

	public void render(Camera cam) {
		shader.use();
		cam.apply(shader);
		material.apply(shader);
		glBindVertexArray(VAO);
		glDrawElementsInstanced(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0, amount);
		glBindVertexArray(0);
	}
}
