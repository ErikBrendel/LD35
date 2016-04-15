/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import util.Animation.Keyframe;
import util.Animation.Vertex;

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
		System.err.println("[loadingLog] Loaded Object with " + indexMap.size() + " vertices " + "and " + indizesList.size() / 3 + " faces");

		int[] vertexDataSizes = {3, 3, 2};
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
		System.err.println("[loadingLog] Loaded Object with " + vertices.size() + " vertices and " + data.size() / 8 / 3 + " faces");
		return dataArray;
	}

	/**
	 * To use with the Animation Vertex shader. Be sure to set BONE_COUNT to the
	 * right values
	 *
	 * IMPORTANT: The collada file must only contain one single mesh object and
	 * one single armature
	 *
	 * @param objectName
	 * @return
	 */
	public static Animation loadAnimation(String objectName) {
		try {
			URL url = Util.class.getResource("/objects/" + objectName).toURI().toURL();

			//load XML into parser
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(url);
			Element collada = doc.getRootElement();
			Namespace ns = collada.getNamespace();
			Animation animatedMesh = new Animation();
			
			//extract Mesh/per_Vertex data
			Element mesh = collada.getChild("library_geometries", ns).getChild("geometry", ns).getChild("mesh", ns);
			Element polylist = mesh.getChild("polylist", ns);
			String[] polyData = polylist.getChild("p", ns).getText().split(" ");
			int polycount = Integer.valueOf(polylist.getAttributeValue("count"));

			String posElemName = mesh.getChild("vertices", ns).getChild("input", ns).getAttributeValue("source").substring(1);
			Element posElem = mesh.getChildren("source", ns).stream().filter((Element e) -> e.getAttributeValue("id").equals(posElemName)).findFirst().get().getChild("float_array", ns);
			String[] posData = posElem.getText().split(" ");

			String normElemName = polylist.getChildren("input", ns).stream().filter((Element e) -> e.getAttributeValue("semantic").equals("NORMAL")).findFirst().get().getAttributeValue("source").substring(1);
			Element normElem = mesh.getChildren("source", ns).stream().filter((Element e) -> e.getAttributeValue("id").equals(normElemName)).findFirst().get().getChild("float_array", ns);
			String[] normData = normElem.getText().split(" ");

			String uvElemName = findAttribChild(polylist, "semantic", "TEXCOORD", "input", ns).getAttributeValue("source").substring(1);
			Element uvElem = findAttribChild(mesh, "id", uvElemName, "source", ns).getChild("float_array", ns);
			String[] uvData = uvElem.getText().split(" ");

			//extract armature/bones data
			Element armature = collada.getChild("library_controllers", ns).getChild("controller", ns).getChild("skin", ns);
			Element bone_weight = armature.getChild("vertex_weights", ns);
			int[] bone_weight_strides = toInt(bone_weight.getChild("vcount", ns).getText().split(" "));
			int[] bone_weight_pointers = toInt(bone_weight.getChild("v", ns).getText().split(" "));

			String weightElemName = findAttribChild(bone_weight, "semantic", "WEIGHT", "input", ns).getAttributeValue("source").substring(1);
			Element weightElem = findAttribChild(armature, "id", weightElemName, "source", ns).getChild("float_array", ns);
			float[] weightData = toFloat(weightElem.getText().split(" "));

			String jointElemName = findAttribChild(bone_weight, "semantic", "JOINT", "input", ns).getAttributeValue("source").substring(1);
			Element jointElem = findAttribChild(armature, "id", jointElemName, "source", ns).getChild("Name_array", ns);
			int bone_count = Integer.valueOf(jointElem.getAttributeValue("count"));

			//
			//
			//
			//build up Vertex objects
			for (int face = 0; face < polycount; face++) {

				for (int edge = 0; edge < 3; edge++) {
					int vp = (face * 3 + edge) * 3; //vertexPointer, holds index of vertex in poly-array

					//fetch basic vertex data
					int posPointer = Integer.valueOf(polyData[vp]);
					int normPointer = Integer.valueOf(polyData[vp + 1]);
					int uvPointer = Integer.valueOf(polyData[vp + 2]);

					Vector3f pos = new Vector3f( //xyz of vertex
							Float.valueOf(posData[posPointer]),
							Float.valueOf(posData[posPointer + 1]),
							Float.valueOf(posData[posPointer + 2]));

					Vector3f norm = new Vector3f( //xyz of normal
							Float.valueOf(normData[normPointer]),
							Float.valueOf(normData[normPointer + 1]),
							Float.valueOf(normData[normPointer + 2]));

					Vector2f uv = new Vector2f( //uv data
							Float.valueOf(uvData[uvPointer]),
							Float.valueOf(uvData[uvPointer + 1]));

					Vertex vert = new Vertex(pos, norm, uv);

					//fetch bone weights
					int simple_vertex_id = posPointer;
					int offset = 0; //add up all offsets from before
					for (int before = 0; before < simple_vertex_id; before++) {
						offset += bone_weight_strides[before];
					}
					int dataCount = bone_weight_strides[simple_vertex_id];
					float[] weights = new float[bone_count];
					for (int weightpp = offset * 2; weightpp < (offset + dataCount) * 2; weightpp += 2) {
						int boneid = bone_weight_pointers[weightpp];
						int weightp = bone_weight_pointers[weightpp + 1];
						weights[boneid] =
								weightData[weightp];
					}
					for (int i = 0; i < weights.length; i++) {
						vert.getBoneWeights().add(weights[i]);
					}

					//finally add the vertex to the animation
					//the animation object automatically creates an index list and drops vectors with identical values
					// (except for weight-data, these arent checked for identification)
					animatedMesh.addVertex(vert);
				}

			}
			
			//
			//
			//
			//
			//
			//
			// Now the animations
			List<Element> animations = collada.getChild("library_animations", ns).getChildren("animation", ns);
			//just assuming that the first occuring animation object belongs to the first occuring bone
			for (int bone = 0; bone < animations.size(); bone++) {
				Element animation = animations.get(bone);
				Element pointerElem = animation.getChild("sampler", ns);
				String timestampsElemName = findAttribChild(pointerElem, "semantic", "INPUT", "input", ns).getAttributeValue("source").substring(1);
				Element timestampsElem = findAttribChild(animation, "id", timestampsElemName, "source", ns).getChild("float_array", ns);
				float[] timestampsData = toFloat(timestampsElem.getText().split(" "));
				
				String transformElemName = findAttribChild(pointerElem, "semantic", "OUTPUT", "input", ns).getAttributeValue("source").substring(1);
				Element transformElem = findAttribChild(animation, "id", transformElemName, "source", ns).getChild("float_array", ns);
				float[] transformData = toFloat(transformElem.getText().split(" "));
				
				for (int key = 0; key < timestampsData.length; key++) {
					//create all keyframes assoc. with this bone
					Keyframe keyframe = new Keyframe(timestampsData[key]);
					float[] keyTransform = new float[16];
					for (int p = 0; p < 16; p++) {
						keyTransform[p] = transformData[(key * 16) + p];
					}
					Matrix4f transform = new Matrix4f();
					transform.loadData(keyTransform);
					keyframe.getBones().put(bone, transform);
					animatedMesh.addKeyframe(keyframe);
				}
			}
			
			
			
			System.err.println("Loaded Object: " + animatedMesh);
			
			
			return animatedMesh;
			/**/
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Error loading Collada file " + objectName + ":\n");
		}
		return null;
	}

	private static int[] toInt(String[] data) {
		int[] ret = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			ret[i] = Integer.valueOf(data[i]);
		}
		return ret;
	}

	private static float[] toFloat(String[] data) {
		float[] ret = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			ret[i] = Float.valueOf(data[i]);
		}
		return ret;
	}

	private static Element findAttribChild(Element father, String attrib, String value) {
		return findAttribChild(father, attrib, value, null, null);
	}

	private static Element findAttribChild(Element father, String attrib, String value, String prefilter, Namespace ns) {
		if (prefilter == null) {
			return father.getChildren().stream().filter((Element e) -> e.getAttributeValue(attrib).equals(value)).findFirst().get();
		} else {
			return father.getChildren(prefilter, ns).stream().filter((Element e) -> e.getAttributeValue(attrib).equals(value)).findFirst().get();
		}
	}
}
