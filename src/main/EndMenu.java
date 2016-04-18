package main;

import java.awt.Font;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.TrueTypeFont;

import util.Material;
import util.Mesh;
import util.MeshInstance;
import util.ObjectLoader;
import util.Shader;
import util.Util;

public class EndMenu {

	private static Mesh icon;
	private static Material backgroundMaterial;
	private static TrueTypeFont font;
	private static final boolean text_AntiAlias = true;

	private MeshInstance background;
	private Shader shader;

	static {
		icon = ObjectLoader.loadObjectEBO("icon.obj");
		backgroundMaterial = new Material(Util.loadTexture("deathscreen.png"), 0);
		Font awtFont = new Font("Times New Roman", Font.BOLD, 40);
		// font = new TrueTypeFont(awtFont, text_AntiAlias);
	}

	public EndMenu() {
		shader = Shader.fromFile("GUI.vert", "GUI.frag");
		background = new MeshInstance(icon, backgroundMaterial);
		background.setScale(new Vector3f(1f, 1f, 1f));
	}

	public int update() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_RETURN) || Keyboard.isKeyDown(Keyboard.KEY_NUMPADENTER)) {
			SpaceScene.playSound("e_apply");
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
