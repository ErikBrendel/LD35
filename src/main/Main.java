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
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Main class for LD project
 */
public class Main {

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
        
        glEnable(GL_DEPTH_TEST);
        
        //game loop
        while (!Display.isCloseRequested()) {
            //rendering
            
            //finish frame
            Display.update();
            Display.sync(60);
        }
    }
}
