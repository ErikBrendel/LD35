package light;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.util.vector.Vector3f;

import com.sun.prism.impl.BufferUtil;

import util.Matrix4f;
import util.Scene;
import util.Shader;

/**
 *
 * @author Erik
 */
public class DirectionalLight extends Light {

	private static final int SHADOW_WIDTH = 1024, SHADOW_HEIGHT = 1024;

	private int depthMapFBO;
	private int depthMap;

	private Matrix4f view, projection, lightSpace;

	private Vector3f direction;

	public DirectionalLight(Color color, Vector3f direction) {
		super(color);
		depthMapFBO = glGenBuffers();

		depthMap = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depthMap);
		// TODO LOOK IF THIS IS ACTUALLY CORRECT
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, BufferUtil.newByteBuffer(0));

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap, 0);
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		this.direction = direction;

		view = util.Util.lookAt(util.Util.vScale(direction, -10), direction, new Vector3f(0, 1, 0));
		projection = util.Util.orthographic(-10, 10, -10, 10, 1.0f, 20.0f);
		Matrix4f.mul(projection, view, lightSpace);
	}

	public DirectionalLight(Vector3f color, Vector3f direction) {
		super(color);
		depthMapFBO = glGenBuffers();

		depthMap = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depthMap);
		// TODO LOOK IF THIS IS ACTUALLY CORRECT
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, BufferUtil.newByteBuffer(0));

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap, 0);
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		this.direction = direction;
	}

	@Override
	public void renderShadows(Scene scene) {
		shadowShader.use();
		glUniformMatrix4(shadowShader.getUniform("lightSpaceMatrix"), false, lightSpace.getData());

		glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
		glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
		glClear(GL_DEPTH_BUFFER_BIT);
		Shader[] shaders = new Shader[3];
		shaders[0] = shadowShader;
		shaders[0] = shadowShader;
		shaders[0] = shadowShader;

		scene.render(shaders);
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	@Override
	public void apply(Shader shader, String uniform) {
		glUniform3f(shader.getUniform(uniform + ".direction"), direction.x, direction.y, direction.z);
		super.apply(shader, uniform);
	}

	@Override
	public float[] getData() {
		float[] data = new float[8];
		data[0] = direction.x;
		data[1] = direction.y;
		data[2] = direction.z;

		data[4] = color.x;
		data[5] = color.y;
		data[6] = color.z;

		return data;
	}
}
