/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.util.ArrayList;
import light.Light;
import light.LightHandler;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * A world is a collection of information to render an image
 *
 * @author Erik
 */
public class World {

	private ArrayList<MeshInstance> objects;
	private LightHandler lh;
	private ArrayList<Shader> shaders;
	private Skybox skybox;
	private Player player;

	public World() {
		lh = new LightHandler();
		objects = new ArrayList<>();
		shaders = new ArrayList<>();
	}

	public void addObject(MeshInstance obj) {
		objects.add(obj);
	}

	public void addLight(Light l) {
		lh.addLight(l, shaders);
	}

	public void addShader(Shader s) {
		shaders.add(s);
	}

	public void setSkybox(Skybox sb) {
		this.skybox = sb;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	/**
	 * renders an openGL frame. Be sure to set Projection and View matrix before
	 */
	public void render() {
		for (MeshInstance i : objects) {
			i.render(shaders.get(0));
		}
		if (skybox != null) {
			glUniform1i(shaders.get(0).getUniform("skybox"), skybox.getTexture());
			skybox.render(player.getCamera());
		}
	}

	public void update(float deltaTime) {
		player.update(deltaTime);
		// update player position uniform
		player.applyToShader(shaders.get(0), false);
	}

	public void updateLight(Light sunLight) {
		lh.updateLight(sunLight);
	}
}
