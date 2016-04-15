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

	public Matrix4f(org.lwjgl.util.vector.Matrix4f src) {
		super(src);
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

	public void invalidate() {
		matrixBuffer = null;
	}
}
