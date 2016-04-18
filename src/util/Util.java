/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Erik
 */
public class Util {

	public static int loadTexture(String name) {
		return loadTexture(name, true);
	}

	public static int loadTexture(String name, boolean interpolate) {
		try {
			URL url = Util.class.getResource("/tex/" + name).toURI().toURL();
			BufferedImage img = ImageIO.read(url);
			return loadTexture(img, interpolate);

		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static int loadTexture(BufferedImage image) {
		return loadTexture(image, true);
	}

	public static int loadTexture(BufferedImage image, boolean interpolate) {

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

		int texture = glGenTextures();
		glActiveTexture(GL_TEXTURE0 + texture);

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

	public static int loadCubeMap(String name) {
		try {
			BufferedImage[] img = new BufferedImage[6];
			img[0] = ImageIO.read(Util.class.getResource("/tex/" + name + "_r.jpg").toURI().toURL());
			img[1] = ImageIO.read(Util.class.getResource("/tex/" + name + "_l.jpg").toURI().toURL());
			img[2] = ImageIO.read(Util.class.getResource("/tex/" + name + "_top.jpg").toURI().toURL());
			img[3] = ImageIO.read(Util.class.getResource("/tex/" + name + "_bot.jpg").toURI().toURL());
			img[4] = ImageIO.read(Util.class.getResource("/tex/" + name + "_b.jpg").toURI().toURL());
			img[5] = ImageIO.read(Util.class.getResource("/tex/" + name + "_f.jpg").toURI().toURL());
			return loadCubeMap(img);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static int loadCubeMap(BufferedImage[] images) {

		int texture = glGenTextures();
		glActiveTexture(GL_TEXTURE_CUBE_MAP + texture);

		for (int i = 0; i < images.length; i++) {
			// fetch all color data from image to array
			int[] pixels = new int[images[i].getWidth() * images[i].getHeight()];
			images[i].getRGB(0, 0, images[i].getWidth(), images[i].getHeight(), pixels, 0, images[i].getWidth());

			// create the openGL Buffer object
			ByteBuffer buffer = BufferUtils.createByteBuffer(images[i].getWidth() * images[i].getHeight() * (images[i].getType() == BufferedImage.TYPE_INT_ARGB ? 4 : 4));

			// copy data to the buffer
			for (int y = 0; y < images[i].getHeight(); y++) {
				for (int x = 0; x < images[i].getWidth(); x++) {
					int pixel = pixels[y * images[i].getWidth() + x];
					buffer.put((byte) (pixel >> 16 & 0xFF)); // Red component
					buffer.put((byte) (pixel >> 8 & 0xFF)); // Green component
					buffer.put((byte) (pixel & 0xFF)); // Blue component
					buffer.put((byte) (pixel >> 24 & 0xFF)); // Alpha component.
					// Only for RGBA
				}
			}
			buffer.flip();
			glBindTexture(GL_TEXTURE_2D, texture);

			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

			glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8, images[i].getWidth(), images[i].getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			if (Settings.getBoolean("skybox_mipmap")) {
				glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
			}
		}

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

	public static Vector3f vmMult(Vector3f v, Matrix4f m) {
		return new Vector3f(v.x * m.m00 + v.y * m.m01 + v.z * m.m02, v.x * m.m10 + v.y * m.m11 + v.z * m.m12, v.x * m.m20 + v.y * m.m21 + v.z * m.m22);
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
					if (current.getWidth() <= 1920 && current.getHeight() <= 1080) {
						full = current;
					}
				}

			}
			return full;
		} catch (Exception ex) {
			return null;
		}
	}

	public static void createWindow(String title, boolean vSync) {
		Point windowSize;
		try {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			windowSize = new Point((int) screen.getWidth(), (int) screen.getHeight());
			Display.setDisplayMode(getBestDisplayMode());
			Display.setFullscreen(true);
			Display.setVSyncEnabled(vSync);
			Display.setTitle(title);
			Display.create();
			Display.setFullscreen(true);

			Mouse.create();
			Mouse.setGrabbed(true);

			glEnable(GL_DEPTH_TEST);
			glDepthFunc(GL_LEQUAL);
			glEnable(GL_STENCIL_TEST);
			// glEnable(GL_FRAMEBUFFER_SRGB);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

			glViewport(0, 0, windowSize.x, windowSize.y);
		} catch (Exception e) {
			System.out.println("Error setting up display");
			System.exit(-1);
		}
	}
}
