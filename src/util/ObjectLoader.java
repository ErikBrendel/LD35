/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Erik
 */
public class ObjectLoader {

    public static float[] loadObject(String objectName) {
        String scanData;
        try {
            URL shaderURL = Util.class.getResource("/res/" + objectName).toURI().toURL();
            Scanner sc = new Scanner(shaderURL.openStream(), "UTF-8");
            scanData = sc.useDelimiter("\\A").next();
            sc.close();
        } catch (Exception ex) {
            System.err.println("Unable to load object file \"" + objectName + "\"");
            return null;
        }
        String[] lines = scanData.split("\\r?\\n");

        //setup lists of raw data
        ArrayList<Vector3f> vertices = new ArrayList<>();
        ArrayList<Vector2f> texCoords = new ArrayList<>();
        ArrayList<Vector3f> normals = new ArrayList<>();
        ArrayList<Float> data = new ArrayList<>();

        //begin loading data into arraylist
        for (String line : lines) {
            if (line.equals("") || line.startsWith("#")) {
                //ignore this line
                continue;
            }
            if (line.startsWith("v ")) {
                //"v x y z"
                //we have plain coordinate data (a vert)
                String[] lineData = line.substring(2).split(" ");
                vertices.add(new Vector3f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])));
                continue;
            }
            if (line.startsWith("vt ")) {
                //"vt u v"
                //here we have texture coordinates for one (still unknown) vert
                String[] lineData = line.substring(3).split(" ");
                texCoords.add(new Vector2f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1])));
                continue;
            }
            if (line.startsWith("vn ")) {
                //"vn x y z"
                //we have a normal vector
                String[] lineData = line.substring(3).split(" ");
                normals.add(new Vector3f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])));
                continue;
            }
            if (line.startsWith("f ")) {
                //"f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3"
                //here we have a face. Now it gets hardcore
                String[] triangleVerts = line.substring(2).split(" ");
                for (int v = 0; v < 3; v++) {
                    String[] vData = triangleVerts[v].split("/");
                    if(vData.length < 3) {
                        System.err.println("unsufficient data: " + line);
                    }
                    int location = Integer.valueOf(vData[0]) - 1;
                    int tex = vData[1].equals("") ? -1 : Integer.valueOf(vData[1]) - 1;
                    int norm = Integer.valueOf(vData[2]) - 1;

                    Vector3f locData = vertices.get(location);
                    Vector2f texData = tex == -1 ? new Vector2f(locData.x, locData.y) : texCoords.get(tex);
                    Vector3f normData = normals.get(norm);

                    data.add(locData.x);
                    data.add(locData.y);
                    data.add(locData.z);
                    data.add(normData.x);
                    data.add(normData.y);
                    data.add(normData.z);
                    data.add(texData.x);
                    data.add(1 - texData.y);
                }
            }
        }

        //convert arrayLists into my own floatArray-Format
        float[] dataArray = new float[data.size()];
        for (int i = 0; i < dataArray.length; i++) {
            dataArray[i] = data.get(i);
        }
                System.err.println("[loadingLog] Loaded Object with " + vertices.size() + " vertices and " + (data.size()/8/3) + " faces");
        return dataArray;
    }
}
