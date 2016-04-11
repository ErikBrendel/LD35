/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 *
 * @author Erik
 */
public class Object {
    
    public static int stride = 8 * 4;
    
    private float[] data;
    
    public Object(String filename) {
        this(ObjectLoader.loadObject(filename));
    }
    
    public Object(float[] allData) {
        this.data = allData;
    }

    /**
     * this method loads the objects vertex data into a given VBO. Please generate the
     * vbo before. The data consist of vec3 position, vec3 normal, vec2 texCoord
     *
     * This function uses GL_ARRAY_BUFFER, so make sure no other VBO is bound to
     * this currently
     *
     * @param VBO the VBO
     */
    public void loadToBuffer(int VBO) {
        FloatBuffer vertexB = BufferUtils.createFloatBuffer(data.length).put(data);
        vertexB.flip();

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexB, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * 4);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6 * 4);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    public int getVertCount() {
        return data.length;
    }
}
