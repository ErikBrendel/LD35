/*
 *  Copyright 2016
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package main;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
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

        // game loop
        while (!Display.isCloseRequested()) {

            long currentFrame = System.nanoTime();
            deltaTime = (float) ((double) (currentFrame - lastFrame) / 1000000d / 1000d);
            //System.err.println("deltaTime = " + deltaTime);
            lastFrame = currentFrame;

            defaultShader.use();
            handleInputs(deltaTime, defaultShader);
            
            
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
