package util;

import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.Player;
import static util.ObjectLoader.loadObjectEBO;

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
		mesh = loadObjectEBO("icon.obj");
		GUIShader = Shader.fromFile("GUI.vert", "GUI.frag");
		this.player = player;
		addIcon("icon_bird_active", true);
		addIcon("icon_bird_disabled", false);
		addIcon("icon_shark_active", true);
		addIcon("icon_shark_disabled", false);
		addIcon("icon_leo_active", true);
		addIcon("icon_leo_disabled", false);
		addIcon("icon_e", true);
		addIcon("icon_q", true);
		for (MeshInstance m : meshes) {
			m.setScale(new Vector3f(0.1f, 0.1f * 16f / 9f, 0.1f));
		}
		meshes.get(6).setLocation(new Vector3f(0.8f, 0.8f, 1));
		meshes.get(7).setLocation(new Vector3f(-0.8f, 0.8f, 1));
	}

	private void addIcon(String icon, boolean visible) {
		meshes.add(new MeshInstance(mesh, new Material(Util.loadTexture(icon + ".png"), 0), visible));
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
					setLeoPosition(position.right);
					setLeo(true);
					setSharkPosition(position.left);
					setShark(false);
				} else if (player.getCurrentMesh() == 1) {
					setSharkPosition(position.disabled);
					setBirdPosition(position.right);
					setBird(true);
					setLeoPosition(position.left);
					setLeo(true);
				} else {
					setLeoPosition(position.disabled);
					setBirdPosition(position.left);
					setBird(true);
					setSharkPosition(position.right);
					setShark(false);
				}
			} else {
				if (player.getCurrentMesh() == 0) {
					setBirdPosition(position.disabled);
					setLeoPosition(position.right);
					setLeo(false);
					setSharkPosition(position.left);
					setShark(true);
				} else if (player.getCurrentMesh() == 1) {
					setSharkPosition(position.disabled);
					setBirdPosition(position.right);
					setBird(true);
					setLeoPosition(position.left);
					setLeo(false);
				} else {
					setLeoPosition(position.disabled);
					setBirdPosition(position.left);
					setBird(true);
					setSharkPosition(position.right);
					setShark(true);
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
				meshes.get(0).setLocation(new Vector3f(-0.8f, 0.8f, 1));
				meshes.get(1).setLocation(new Vector3f(-0.8f, 0.8f, 1));
				break;
			case right:
				meshes.get(0).setLocation(new Vector3f(0.8f, 0.8f, 1));
				meshes.get(1).setLocation(new Vector3f(0.8f, 0.8f, 1));
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
				meshes.get(2).setLocation(new Vector3f(-0.8f, 0.8f, 1));
				meshes.get(3).setLocation(new Vector3f(-0.8f, 0.8f, 1));
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
				meshes.get(4).setLocation(new Vector3f(-0.8f, 0.8f, 1));
				meshes.get(5).setLocation(new Vector3f(-0.8f, 0.8f, 1));
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

	public void render() {
		GUIShader.use();
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		for (MeshInstance m : meshes) {
			m.render(GUIShader);
		}
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
	}
}
