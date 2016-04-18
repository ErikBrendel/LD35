package main;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import util.Material;
import util.Mesh;
import util.MeshInstance;
import util.ObjectLoader;
import util.Shader;
import util.Util;

public class MainMenu {

	private int cursorPos;
	private Vector3f cursorLocation;
	private MeshInstance background;
	private MeshInstance cursor;

	private Shader shader;

	private static Mesh icon;
	private static Material backgroundMaterial;
	private static Material cursorMaterial;

	private float timeSinceLastButtonPress;
	private float timePassed;

	private boolean allowContinue;

	private boolean open;

	static {
		icon = ObjectLoader.loadObjectEBO("icon.obj");
		backgroundMaterial = new Material(Util.loadTexture("menu.png"), 0);
		cursorMaterial = new Material(Util.loadTexture("cursor.png"), 0);
	}

	public MainMenu() {
		shader = Shader.fromFile("GUI.vert", "GUI.frag");
		cursorPos = 1;
		cursorLocation = new Vector3f(-0.4f, 0.26f * (cursorPos + 1) - 0.064f, 1);
		cursor = new MeshInstance(icon, cursorMaterial);
		cursor.setLocation(cursorLocation);
		background = new MeshInstance(icon, backgroundMaterial);
		background.setScale(new Vector3f(1f, 1f, 1f));
		cursor.setScale(new Vector3f(0.05f * 1f, 0.05f * 16f / 9f, 0.05f * 1f));
		open = true;
		allowContinue = false;
	}

	public void setAllowContinue(boolean allowContinue) {
		this.allowContinue = allowContinue;
		if (!allowContinue) {
			setCursorPos(1);
		}
	}

	public void setCursorPos(int cursorPos) {
		this.cursorPos = cursorPos;
		cursorLocation = new Vector3f(-0.4f, 0.26f * (cursorPos + 1) - 0.064f, 1);
		cursor.setLocation(cursorLocation);
	}

	public int update(float deltaTime) {
		if (!open) {
			return cursorPos;
		}

		timeSinceLastButtonPress += deltaTime;
		timePassed += deltaTime;
		if (timeSinceLastButtonPress > 0.2f) {
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				Display.destroy();
				System.exit(0);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				up();
				timeSinceLastButtonPress = 0;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				down();
				timeSinceLastButtonPress = 0;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_NUMPADENTER) || Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
				SpaceScene.playSound("e_apply");
				open = false;
				return cursorPos;
			}
		}
		cursor.setScale(new Vector3f(0.05f * (float) (Math.sin(timePassed * 2) / 8 + 0.8), 0.05f * 16f / 9f * (float) (Math.sin(timePassed * 2) / 8 + 0.8), 0.05f * 1f / (float) (Math.sin(timePassed * 2) / 8 + 0.8)));

		return -1;
	}

	public void setOpen(boolean open) {
		this.open = open;
		timeSinceLastButtonPress = -0.2f;
	}

	public void render() {
		shader.use();
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		background.render(shader);
		cursor.render(shader);
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
	}

	private void up() {
		cursorPos++;
		cursorPos %= 3;
		if (!allowContinue) {
			cursorPos %= 2;
		}
		cursorLocation = new Vector3f(-0.4f, 0.26f * (cursorPos + 1) - 0.064f, 1);
		cursor.setLocation(cursorLocation);
		SpaceScene.playSound("e_select");
	}

	private void down() {
		cursorPos--;
		if (cursorPos < 0) {
			cursorPos = 2;
			if (!allowContinue) {
				cursorPos = 1;
			}
		}
		cursorLocation = new Vector3f(-0.4f, 0.26f * (cursorPos + 1) - 0.064f, 1);
		cursor.setLocation(cursorLocation);
		SpaceScene.playSound("e_select");
	}
}
