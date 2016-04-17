/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Erik
 */
public class ObjectLoader {

	public static Mesh loadObjectEBO(String objectName) {
		String scanData;
		try {
			URL shaderURL = Util.class.getResource("/objects/" + objectName).toURI().toURL();
			Scanner sc = new Scanner(shaderURL.openStream(), "UTF-8");
			scanData = sc.useDelimiter("\\A").next();
			sc.close();
		} catch (Exception ex) {
			System.err.println("Unable to load object file \"" + objectName + "\"");
			return null;
		}
		String[] lines = scanData.split("\\r?\\n");

		// setup lists of raw data
		ArrayList<Vector3f> vertices = new ArrayList<>();
		ArrayList<Vector2f> texCoords = new ArrayList<>();
		ArrayList<Vector3f> normals = new ArrayList<>();
		ArrayList<float[]> dataList = new ArrayList<>();
		HashMap<String, Integer> indexMap = new HashMap<>();
		ArrayList<Integer> indizesList = new ArrayList<>();

		// begin loading data into arraylist
		for (String line : lines) {
			if (line.equals("") || line.startsWith("#")) {
				// ignore this line
				continue;
			}
			if (line.startsWith("v ")) {
				// "v x y z"
				// we have plain coordinate data (a vert)
				String[] lineData = line.substring(2).split(" ");
				vertices.add(new Vector3f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])));
				continue;
			}
			if (line.startsWith("vt ")) {
				// "vt u v"
				// here we have texture coordinates for one (still unknown) vert
				String[] lineData = line.substring(3).split(" ");
				texCoords.add(new Vector2f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1])));
				continue;
			}
			if (line.startsWith("vn ")) {
				// "vn x y z"
				// we have a normal vector
				String[] lineData = line.substring(3).split(" ");
				normals.add(new Vector3f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])));
				continue;
			}
			if (line.startsWith("f ")) {
				// "f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3"
				// here we have a face. Now it gets hardcore
				String[] triangleVerts = line.substring(2).split(" ");
				for (int v = 0; v < 3; v++) {
					String vertDataRaw = triangleVerts[v];
					Integer index = indexMap.get(vertDataRaw);
					if (index == null) {
						// first occurence, create data array
						String[] vData = vertDataRaw.split("/");
						if (vData.length != 3) {
							System.err.println("unsufficient data: " + line);
						}
						int location = Integer.valueOf(vData[0]) - 1;
						int tex = vData[1].equals("") ? -1 : Integer.valueOf(vData[1]) - 1;
						int norm = Integer.valueOf(vData[2]) - 1;

						Vector3f locData = vertices.get(location);
						Vector2f texData = tex == -1 ? new Vector2f(locData.x, locData.y) : texCoords.get(tex);
						Vector3f normData = normals.get(norm);

						float[] data = new float[8];
						data[0] = locData.x;
						data[1] = locData.y;
						data[2] = locData.z;
						data[3] = normData.x;
						data[4] = normData.y;
						data[5] = normData.z;
						data[6] = texData.x;
						data[7] = 1 - texData.y;
						dataList.add(data);
						index = dataList.size() - 1;
						indexMap.put(vertDataRaw, index);
					}
					indizesList.add(index);
				}
			}
		}

		// actual mesh data
		float[] data = new float[dataList.size() * 8];
		for (int i = 0; i < dataList.size(); i++) {
			for (int off = 0; off < 8; off++) {
				data[i * 8 + off] = dataList.get(i)[off];
			}
		}

		// actual mash indizes
		int[] indizes = new int[indizesList.size()];
		for (int i = 0; i < indizesList.size(); i++) {
			indizes[i] = indizesList.get(i);
		}

		/*
		 * DEBUG STUFF System.err.println("Data: "); for (int i = 0; i <
		 * data.length; i++) { System.err.println(data[i]); }
		 * System.err.println("\nMap: "); indexMap.forEach((String str, Integer
		 * i) -> System.err.println("Map: " + str + " -> " + i));
		 * System.err.println("\nIndizes: ");
		 * indizesList.forEach(System.err::println);/*
		 */
		System.err.println("[loadingLog] Loaded Object " + objectName + " with " + indexMap.size() + " vertices " + "and " + indizesList.size() / 3 + " faces");

		int[] vertexDataSizes = { 3, 3, 2 };
		return new Mesh(data, indizes, vertexDataSizes);
	}

	public static float[] loadObject(String objectName) {
		String scanData;
		try {
			URL shaderURL = Util.class.getResource("/objects/" + objectName).toURI().toURL();
			Scanner sc = new Scanner(shaderURL.openStream(), "UTF-8");
			scanData = sc.useDelimiter("\\A").next();
			sc.close();
		} catch (Exception ex) {
			System.err.println("Unable to load object file \"" + objectName + "\"");
			return null;
		}
		String[] lines = scanData.split("\\r?\\n");

		// setup lists of raw data
		ArrayList<Vector3f> vertices = new ArrayList<>();
		ArrayList<Vector2f> texCoords = new ArrayList<>();
		ArrayList<Vector3f> normals = new ArrayList<>();
		ArrayList<Float> data = new ArrayList<>();

		// begin loading data into arraylist
		for (String line : lines) {
			if (line.equals("") || line.startsWith("#")) {
				// ignore this line
				continue;
			}
			if (line.startsWith("v ")) {
				// "v x y z"
				// we have plain coordinate data (a vert)
				String[] lineData = line.substring(2).split(" ");
				vertices.add(new Vector3f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])));
				continue;
			}
			if (line.startsWith("vt ")) {
				// "vt u v"
				// here we have texture coordinates for one (still unknown) vert
				String[] lineData = line.substring(3).split(" ");
				texCoords.add(new Vector2f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1])));
				continue;
			}
			if (line.startsWith("vn ")) {
				// "vn x y z"
				// we have a normal vector
				String[] lineData = line.substring(3).split(" ");
				normals.add(new Vector3f(Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])));
				continue;
			}
			if (line.startsWith("f ")) {
				// "f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3"
				// here we have a face. Now it gets hardcore
				String[] triangleVerts = line.substring(2).split(" ");
				for (int v = 0; v < 3; v++) {
					String[] vData = triangleVerts[v].split("/");
					if (vData.length < 3) {
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

		// convert arrayLists into my own floatArray-Format
		float[] dataArray = new float[data.size()];
		for (int i = 0; i < dataArray.length; i++) {
			dataArray[i] = data.get(i);
		}
		System.err.println("[loadingLog] Loaded Object " + objectName + " with " + vertices.size() + " vertices and " + data.size() / 8 / 3 + " faces");
		return dataArray;
	}
}
