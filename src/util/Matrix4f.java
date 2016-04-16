/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Erik
 */
public class Matrix4f extends org.lwjgl.util.vector.Matrix4f {

	private FloatBuffer matrixBuffer = null;
	private float[] data = null;

	public Matrix4f() {
		super();
	}

	public FloatBuffer getData() {
		if (matrixBuffer == null) {
			matrixBuffer = BufferUtils.createFloatBuffer(16);
			this.store(matrixBuffer);
			matrixBuffer.flip();
		}
		return matrixBuffer;
	}

	public float[] getDataArray() {
		if (data == null) {
			float[] d = { m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33 };
			data = d;
		}
		return data;
	}
	
	public void loadData(float[] data) {
		assert data.length >= 16;
		m00 = data[0];
		m01 = data[1];
		m02 = data[2];
		m03 = data[3];
		m10 = data[4];
		m11 = data[5];
		m12 = data[6];
		m13 = data[7];
		m20 = data[8];
		m21 = data[9];
		m22 = data[10];
		m23 = data[11];
		m30 = data[12];
		m31 = data[13];
		m32 = data[14];
		m33 = data[15];
		
		getDataArray();
	}

	public Matrix4f invalidate() {
		matrixBuffer = null;
		return this;
	}
}
