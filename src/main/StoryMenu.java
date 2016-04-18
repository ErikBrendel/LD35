package main;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import util.Material;
import util.Mesh;
import util.MeshInstance;
import util.ObjectLoader;
import util.Shader;
import util.Util;

public class StoryMenu {

	private static Mesh icon;
	private static Material backgroundMaterial;

	private MeshInstance background;
	private Shader shader;

	private boolean firstStart;

	static {
		icon = ObjectLoader.loadObjectEBO("icon.obj");
		backgroundMaterial = new Material(Util.loadTexture("story.png"), 0);
	}

	public StoryMenu() {
		shader = Shader.fromFile("GUI.vert", "GUI.frag");
		background = new MeshInstance(icon, backgroundMaterial);
		background.setScale(new Vector3f(1f, 1f, 1f));
		firstStart = true;
	}

	public int update() {
		if (!firstStart) {
			return 0;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_RETURN) || Keyboard.isKeyDown(Keyboard.KEY_NUMPADENTER)) {
			SpaceScene.playSound("e_apply");
			firstStart = false;
			return 0;
		}
		return -1;
	}

	public void render() {
		shader.use();
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		background.render(shader);
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
		// Color.white.bind();
		// font.drawString(100, 50, "THE LIGHTWEIGHT JAVA GAMES LIBRARY",
		// Color.white);
	}
}
