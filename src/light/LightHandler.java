package light;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import util.Scene;
import util.Shader;

import com.sun.prism.impl.BufferUtil;

import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.*;

public class LightHandler {
	private int numPointLights, numDirLights, numSpotLights;
	private ArrayList<PointLight> pointLights;
	private ArrayList<DirectionalLight> dirLights;
	private ArrayList<SpotLight> spotLights;

	private int UBO;

	public LightHandler() {
		numDirLights = 0;
		numPointLights = 0;
		numSpotLights = 0;
		pointLights = new ArrayList<>();
		dirLights = new ArrayList<>();
		spotLights = new ArrayList<>();
		UBO = glGenBuffers();
		generateUBO();
	}

	private void generateUBO() {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		glBufferData(GL_UNIFORM_BUFFER, 32 * Math.max(1, numDirLights) + 48 * Math.max(1, numPointLights) + 64 * Math.max(1, numSpotLights), GL_DYNAMIC_DRAW);

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, UBO);

		int dirLightOffset = 0;
		for (DirectionalLight dl : dirLights) {
			FloatBuffer dirLightsBuffer = BufferUtil.newFloatBuffer(8);
			float[] data = dl.getData();
			dirLightsBuffer.put(data);
			dirLightsBuffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, dirLightOffset * 4, dirLightsBuffer);
			dirLightOffset += 8;
		}

		int spotLightOffset = 0;
		for (SpotLight sl : spotLights) {
			FloatBuffer spotLightsBuffer = BufferUtil.newFloatBuffer(16);
			float[] data = sl.getData();
			spotLightsBuffer.put(data);
			spotLightsBuffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + spotLightOffset) * 4, spotLightsBuffer);
			spotLightOffset += 16;
		}

		int pointLightOffset = 0;
		for (PointLight pl : pointLights) {
			FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
			float[] data = pl.getData();
			pointLightsBuffer.put(data);
			pointLightsBuffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + pointLightOffset) * 4, pointLightsBuffer);
			pointLightOffset += 12;
		}
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void addLight(Light light, ArrayList<Shader> shaders) {
		if (light instanceof PointLight) {
			pointLights.add((PointLight) light);
			numPointLights++;
		}
		if (light instanceof DirectionalLight) {
			dirLights.add((DirectionalLight) light);
			numDirLights++;
		}
		if (light instanceof SpotLight) {
			spotLights.add((SpotLight) light);
			numSpotLights++;
		}
		for (Shader s : shaders) {
			s.updateParameter("NUM_DIR_LIGHTS", numDirLights, false);
			s.updateParameter("NUM_SPOT_LIGHTS", numSpotLights, false);
			s.updateParameter("NUM_POINT_LIGHTS", numPointLights, false);
			s.recompile();
		}
		generateUBO();
	}

	public void updateLight(Light light) {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		if (light instanceof PointLight) {
			int pointLightOffset = 0;
			for (int i = 0; i < pointLights.size(); i++) {
				if (light.equals(pointLights.get(i))) {
					pointLights.set(i, (PointLight) light);
					FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
					float[] data = light.getData();
					pointLightsBuffer.put(data);
					pointLightsBuffer.flip();
					glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + pointLightOffset) * 4, pointLightsBuffer);
				}
				pointLightOffset += 12;
			}
		}
		if (light instanceof DirectionalLight) {
			int dirLightOffset = 0;
			for (int i = 0; i < dirLights.size(); i++) {
				if (light.equals(dirLights.get(i))) {
					dirLights.set(i, (DirectionalLight) light);
					FloatBuffer dirLightsBuffer = BufferUtil.newFloatBuffer(8);
					float[] data = light.getData();
					dirLightsBuffer.put(data);
					dirLightsBuffer.flip();
					glBufferSubData(GL_UNIFORM_BUFFER, dirLightOffset * 4, dirLightsBuffer);
				}
				dirLightOffset += 8;
			}
		}
		if (light instanceof SpotLight) {
			int spotLightOffset = 0;
			for (int i = 0; i < spotLights.size(); i++) {
				if (light.equals(spotLights.get(i))) {
					spotLights.set(i, (SpotLight) light);
					FloatBuffer spotLightsBuffer = BufferUtil.newFloatBuffer(16);
					float[] data = light.getData();
					spotLightsBuffer.put(data);
					spotLightsBuffer.flip();
					glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(numDirLights, 1) * 8 + spotLightOffset) * 4, spotLightsBuffer);
				}
				spotLightOffset += 16;
			}

		}
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void renderLightShadows(Scene scene) {
		for (DirectionalLight dl : dirLights) {
			dl.renderShadows(scene);
		}

		for (SpotLight sl : spotLights) {
			sl.renderShadows(scene);
		}

		for (PointLight pl : pointLights) {
			pl.renderShadows(scene);
		}

		glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}

	public int getNumDirLights() {
		return numDirLights;
	}

	public int getNumSpotLights() {
		return numSpotLights;
	}

	public int getNumPointLights() {
		return numPointLights;
	}
}
