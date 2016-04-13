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
	
	public void invalidate() {
		matrixBuffer = null;
	}
}
