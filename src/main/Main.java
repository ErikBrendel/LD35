/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import light.PointLight;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import org.lwjgl.util.vector.Vector3f;
import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.Player;
import util.Shader;
import util.Util;

/**
 * Main class for LD project
 */
public class Main {

    static float deltaTime = 0;
    static long lastFrame = 0;

    public static void main(String[] args) {
        System.out.println("test!");

        // create window
        Point windowSize;
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            windowSize = new Point((int) screen.getWidth(), (int) screen.getHeight());
            DisplayMode full = Util.getBestDisplayMode();
            Display.setDisplayMode(full);
            Display.setFullscreen(true);
            Display.setVSyncEnabled(true);
            Display.setTitle("Learning openGL with Java");
            Display.create();

            Mouse.create();
            Mouse.setGrabbed(true);
            glViewport(0, 0, windowSize.x, windowSize.y);
        } catch (Exception e) {
            System.out.println("Error setting up display");
            System.exit(-1);
        }

        Shader defaultShader = Shader.fromFile("default.vert", "default.frag");
        defaultShader.use();
        glEnable(GL_DEPTH_TEST);
        
        Util.loadTexture("minecraft.png", 0);
        Material mat = new Material(0, 0, 32);
        mat.apply(defaultShader);
        
        
        //light
        PointLight pl = new PointLight(Color.yellow, 1, new Vector3f(2, 2, 2), 50);
        pl.apply(defaultShader, "pointLights[0]");

        //bunny
        Mesh bunny = new Mesh("bunny.obj");
        int bunnyVAO = glGenVertexArrays();
        int VBO = glGenBuffers();

        //creating VAOs
        glBindVertexArray(bunnyVAO);
        bunny.loadToBuffer(VBO);
        glBindVertexArray(0);

        Player player = new Player();

        // game loop
        while (!Display.isCloseRequested()) {

            long currentFrame = System.nanoTime();
            deltaTime = (float) ((double) (currentFrame - lastFrame) / 1000000d / 1000d);
            //System.err.println("deltaTime = " + deltaTime);
            lastFrame = currentFrame;

            defaultShader.use();
            handleInputs(deltaTime, defaultShader);

            //render
            glClearColor(0.05f, 0.075f, 0.075f, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f view = player.getViewMatrix();
            glUniformMatrix4(defaultShader.getUniform("view"), false, view.getData());

            Matrix4f projection = player.getProjectionMatrix();
            glUniformMatrix4(defaultShader.getUniform("projection"), false, projection.getData());

            Matrix4f model = new Matrix4f();
            model.translate(new Vector3f(0, 0, -1));
            glUniformMatrix4(defaultShader.getUniform("model"), false, model.getData());

            glBindVertexArray(bunnyVAO);
            glDrawArrays(GL_TRIANGLES, 0, bunny.getVertCount());
            glBindVertexArray(0);

            // finish frame
            Display.update();
            Display.sync(60);
        }
    }

    private static void handleInputs(float deltaTime, Shader defaultShader) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            System.exit(0);
        }
    }
}
