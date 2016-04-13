package light;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import util.Shader;

import com.sun.prism.impl.BufferUtil;

import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL30.*;
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
		glBufferData(GL_UNIFORM_BUFFER, 32 * numDirLights + 48 * numPointLights + 64 * numSpotLights, GL_STATIC_DRAW);

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
			glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(dirLightOffset, 8) + spotLightOffset) * 4, spotLightsBuffer);
			spotLightOffset += 16;
		}

		int pointLightOffset = 0;
		for (PointLight pl : pointLights) {
			FloatBuffer pointLightsBuffer = BufferUtil.newFloatBuffer(12);
			float[] data = pl.getData();
			pointLightsBuffer.put(data);
			pointLightsBuffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, (Math.max(24, dirLightOffset + spotLightOffset) + pointLightOffset) * 4, pointLightsBuffer);
			pointLightOffset += 12;
		}
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		System.out.println(pointLightOffset + " " + spotLightOffset + " " + dirLightOffset);
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
}
