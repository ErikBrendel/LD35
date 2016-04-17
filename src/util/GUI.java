package util;

import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.Player;

public class GUI {

	private ArrayList<MeshInstance> meshes;
	private Mesh mesh;
	private Shader GUIShader;
	private Player player;

	private enum position {
		left, right, disabled
	};

	public GUI(Player player) {
		meshes = new ArrayList<>();
		mesh = ObjectLoader.loadObjectEBO("icon.obj");
		GUIShader = Shader.fromFile("GUI.vert", "GUI.frag");
		this.player = player;
		addIcon("icon_bird_active", true);
		addIcon("icon_bird_disabled", false);
		addIcon("icon_shark_active", true);
		addIcon("icon_shark_disabled", false);
		addIcon("icon_leo_active", true);
		addIcon("icon_leo_disabled", false);
		for (MeshInstance m : meshes) {
			m.setScale(1f);
		}
	}

	private void addIcon(String icon, boolean visible) {
		meshes.add(new MeshInstance(mesh, new Material(icon + ".png", "black.png"), visible));
	}

	public void update() {
		if (player.getCurrentMesh() != player.getNextMesh()) {
			setBird(false);
			setShark(false);
			setLeo(false);
		} else {
			if (player.isOverland()) {
				if (player.getCurrentMesh() == 0) {
					setBirdPosition(position.disabled);
					setLeoPosition(position.left);
					setLeo(true);
					setSharkPosition(position.right);
					setShark(false);
				} else if (player.getCurrentMesh() == 1) {
					setSharkPosition(position.disabled);
					setBirdPosition(position.left);
					setBird(true);
					setLeoPosition(position.right);
					setLeo(true);
				} else {
					setLeoPosition(position.disabled);
					setBirdPosition(position.right);
					setBird(true);
					setSharkPosition(position.left);
					setShark(false);
				}
			} else {
				if (player.getCurrentMesh() == 0) {

				} else if (player.getCurrentMesh() == 1) {

				} else {

				}
			}
		}
	}

	private void setBirdPosition(position pos) {
		switch (pos) {
			case disabled:
				meshes.get(0).setLocation(new Vector3f(-1.9f, -1.9f, 1));
				meshes.get(1).setLocation(new Vector3f(-1.9f, -1.9f, 1));
				break;
			case left:
				meshes.get(0).setLocation(new Vector3f(-0.1f, -0.1f, 1));
				meshes.get(1).setLocation(new Vector3f(-0.1f, -0.1f, 1));
				break;
			case right:
				meshes.get(0).setLocation(new Vector3f(0.1f, 0.1f, 1));
				meshes.get(1).setLocation(new Vector3f(0.1f, 0.1f, 1));
				break;
			default:
				break;
		}
	}

	private void setSharkPosition(position pos) {
		switch (pos) {
			case disabled:
				meshes.get(2).setLocation(new Vector3f(-1.9f, -1.9f, 1));
				meshes.get(3).setLocation(new Vector3f(-1.9f, -1.9f, 1));
				break;
			case left:
				meshes.get(2).setLocation(new Vector3f(-0.9f, -0.9f, 1));
				meshes.get(3).setLocation(new Vector3f(-0.9f, -0.9f, 1));
				break;
			case right:
				meshes.get(2).setLocation(new Vector3f(0.8f, 0.8f, 1));
				meshes.get(3).setLocation(new Vector3f(0.8f, 0.8f, 1));
				break;
			default:
				break;
		}
	}

	private void setLeoPosition(position pos) {
		switch (pos) {
			case disabled:
				meshes.get(4).setLocation(new Vector3f(-1.9f, -1.9f, 1));
				meshes.get(5).setLocation(new Vector3f(-1.9f, -1.9f, 1));
				break;
			case left:
				meshes.get(4).setLocation(new Vector3f(-0.9f, -0.9f, 1));
				meshes.get(5).setLocation(new Vector3f(-0.9f, -0.9f, 1));
				break;
			case right:
				meshes.get(4).setLocation(new Vector3f(0.8f, 0.8f, 1));
				meshes.get(5).setLocation(new Vector3f(0.8f, 0.8f, 1));
				break;
			default:
				break;
		}
	}

	private void setBird(boolean enabled) {
		if (enabled) {
			meshes.get(0).setVisible(true);
			meshes.get(1).setVisible(false);
		} else {
			meshes.get(0).setVisible(false);
			meshes.get(1).setVisible(true);
		}
	}

	private void setShark(boolean enabled) {
		if (enabled) {
			meshes.get(2).setVisible(true);
			meshes.get(3).setVisible(false);
		} else {
			meshes.get(2).setVisible(false);
			meshes.get(3).setVisible(true);
		}
	}

	private void setLeo(boolean enabled) {
		if (enabled) {
			meshes.get(4).setVisible(true);
			meshes.get(5).setVisible(false);
		} else {
			meshes.get(4).setVisible(false);
			meshes.get(5).setVisible(true);
		}
	}

	public void render(Shader s) {
		GUIShader.use();
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		for (MeshInstance m : meshes) {
			m.setVisible(true);
			m.setLocation(new Vector3f(-1, -1, 0));
			m.setScale(0.5f);
			m.render(s);
		}
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
	}
}
