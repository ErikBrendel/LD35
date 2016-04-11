/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Erik
 */
public class Util {

	public static int loadTexture(String name, int id) {
		return loadTexture(name, id, true);
	}

	public static int loadTexture(String name, int id, boolean interpolate) {
		try {
			URL url = Util.class.getResource("/tex/" + name).toURI().toURL();
			BufferedImage img = ImageIO.read(url);
			return loadTexture(img, id, interpolate);

		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static int loadTexture(BufferedImage image, int id) {
		return loadTexture(image, id, true);
	}

	public static int loadTexture(BufferedImage image, int id, boolean interpolate) {

		// fetch all color data from image to array
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		// create the openGL Buffer object
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * (image.getType() == BufferedImage.TYPE_INT_ARGB ? 4 : 4));

		// copy data to the buffer
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) (pixel >> 16 & 0xFF)); // Red component
				buffer.put((byte) (pixel >> 8 & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) (pixel >> 24 & 0xFF)); // Alpha component.
															// Only for RGBA
			}
		}
		buffer.flip();

		glActiveTexture(GL_TEXTURE0 + id);
		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		if (interpolate) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glGenerateMipmap(GL_TEXTURE_2D);

		// glBindTexture(GL_TEXTURE_2D, 0);
		return texture;
	}

	public static Matrix4f perspective(double fov, double aspect, double zNear, double zFar) {
		Matrix4f mat = new Matrix4f();

		double yScale = 1 / Math.tan(Math.toRadians(fov / 2));
		double xScale = yScale / aspect;
		double frustrumLength = zFar - zNear;

		mat.m00 = (float) xScale;
		mat.m11 = (float) yScale;
		mat.m22 = -(float) ((zFar + zNear) / frustrumLength);
		mat.m23 = -1;
		mat.m32 = -(float) (2 * zFar * zNear / frustrumLength);
		mat.m33 = 0;

		return mat;
	}

	public static Vector3f vPlus(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
	}

	public static Vector3f vMinus(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
	}

	public static Vector3f vScale(Vector3f v, float s) {
		return new Vector3f(v.x * s, v.y * s, v.z * s);
	}

	public static Vector3f vCross(Vector3f a, Vector3f b) {
		return new Vector3f(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}

	public static Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up) {
		Vector3f f = (Vector3f) vMinus(center, eye).normalise();
		Vector3f u = (Vector3f) up.normalise();
		Vector3f s = (Vector3f) vCross(f, u).normalise();
		u = vCross(s, f);

		Matrix4f result = new Matrix4f();
		result.m00 = s.x;
		result.m10 = s.y;
		result.m20 = s.z;
		result.m01 = u.x;
		result.m11 = u.y;
		result.m21 = u.z;
		result.m02 = -f.x;
		result.m12 = -f.y;
		result.m22 = -f.z;

		return (Matrix4f) result.translate(new Vector3f(-eye.x, -eye.y, -eye.z));
	}

	public static DisplayMode getBestDisplayMode() {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			DisplayMode full = null;
			// finding best quality display mode
			for (DisplayMode current : modes) {
				if (full == null || current.getWidth() > full.getWidth() || current.getHeight() > full.getHeight() || current.getFrequency() > full.getFrequency() || current.getBitsPerPixel() > full.getBitsPerPixel()) {
					full = current;
				}

			}
			return full;
		} catch (Exception ex) {
			return null;
		}
	}
}
