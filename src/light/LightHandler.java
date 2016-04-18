package light;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import util.Shader;

import com.sun.prism.impl.BufferUtil;

import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL15.*;

public class LightHandler {
	private int numPointLights, numDirLights, numSpotLights;
	private PointLight[] pointLights;
	private ArrayList<DirectionalLight> dirLights;
	private ArrayList<SpotLight> spotLights;
	private LinkedList<Integer> freeSpots;

	private int UBO;

	public LightHandler() {
		freeSpots = new LinkedList<>();
		numDirLights = 0;
		numPointLights = 300;
		for (int i = 0; i < numPointLights; i++) {
			freeSpots.add(i);
		}
		numSpotLights = 0;
		pointLights = new PointLight[numPointLights];
		dirLights = new ArrayList<>();
		spotLights = new ArrayList<>();
		UBO = glGenBuffers();
		generateUBO();
	}

	private void generateUBO() {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		glBufferData(GL_UNIFORM_BUFFER, 32 * Math.max(1, numDirLights) + 48 * Math.max(1, numPointLights) + 64 * Math.max(1, numSpotLights), GL_STATIC_DRAW);

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
		int i = 0;
		for (PointLight pl : pointLights) {
			if (pl != null) {
				i++;
				FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
				float[] data = pl.getData();
				pointLightsBuffer.put(data);
				pointLightsBuffer.flip();
				glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + pointLightOffset) * 4, pointLightsBuffer);
				pointLightOffset += 12;
			}
		}
		for (; i < numPointLights; i++) {
			FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
			float[] data = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 };
			pointLightsBuffer.put(data);
			pointLightsBuffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + pointLightOffset) * 4, pointLightsBuffer);
			pointLightOffset += 12;
		}
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void addLight(Light light, ArrayList<Shader> shaders) {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		if (light instanceof DirectionalLight) {
			dirLights.add((DirectionalLight) light);
			numDirLights++;
		}
		if (light instanceof SpotLight) {
			spotLights.add((SpotLight) light);
			numSpotLights++;
		}

		if (light instanceof PointLight) {
			FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
			float[] data = light.getData();
			pointLightsBuffer.put(data);
			pointLightsBuffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + freeSpots.getFirst() * 12) * 4, pointLightsBuffer);
			pointLights[freeSpots.getFirst()] = (PointLight) light;
			freeSpots.removeFirst();
		} else {
			for (Shader s : shaders) {
				s.updateParameter("NUM_DIR_LIGHTS", numDirLights, false);
				s.updateParameter("NUM_SPOT_LIGHTS", numSpotLights, false);
				s.updateParameter("NUM_POINT_LIGHTS", numPointLights, false);
				s.recompile();
			}
			generateUBO();
		}
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void remLight(Light light, ArrayList<Shader> shaders) {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		if (light instanceof DirectionalLight) {
			for (int i = 0; i < dirLights.size(); i++) {
				if (light.equals(dirLights.get(i))) {
					dirLights.remove(i);
					i--;
					numDirLights--;
				}
			}
		}
		if (light instanceof SpotLight) {
			for (int i = 0; i < spotLights.size(); i++) {
				if (light.equals(spotLights.get(i))) {
					spotLights.remove(i);
					i--;
					numSpotLights--;
				}
			}
		}

		if (light instanceof PointLight) {
			for (int i = 0; i < pointLights.length; i++) {
				if (light.equals(pointLights[i])) {
					FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
					float[] data = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 };
					pointLightsBuffer.put(data);
					pointLightsBuffer.flip();
					glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + i * 12) * 4, pointLightsBuffer);
					freeSpots.add(i);
					pointLights[i] = null;
				}
			}
		} else {
			for (Shader s : shaders) {
				s.updateParameter("NUM_DIR_LIGHTS", numDirLights, false);
				s.updateParameter("NUM_SPOT_LIGHTS", numSpotLights, false);
				s.updateParameter("NUM_POINT_LIGHTS", numPointLights, false);
				s.recompile();
			}
			generateUBO();
		}
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void updateLight(Light light) {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		if (light instanceof PointLight) {
			int pointLightOffset = 0;
			for (int i = 0; i < pointLights.length; i++) {
				if (pointLights[i] != null) {
					if (light.equals(pointLights[i])) {
						pointLights[i] = (PointLight) light;
						FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
						float[] data = light.getData();
						pointLightsBuffer.put(data);
						pointLightsBuffer.flip();
						glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(1, numDirLights) * 8 + Math.max(1, numSpotLights) * 16 + pointLightOffset) * 4, pointLightsBuffer);
					}
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
