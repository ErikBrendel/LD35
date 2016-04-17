/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package generating;

import generating.Graph.GraphNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector3f;
import util.Material;
import util.Mesh;
import util.MeshInstance;
import util.ObjectLoader;
import util.Util;

/**
 * top-level object to generate a random world
 *
 * @author Erik
 */
public class WorldGenerator {

	private static final int SMOOTH_LOOP_COUNT = 3;
	private static final float waterThreshold = 0f;
	private static final float landHeight = 1f;
	private static final float waterHeight = 1f/1.015f;
	private static final float underwaterHeight = 0.94f;

	private Random r;
	private MeshInstance planetObject;

	/**
	 * Create a new generator to get one random world
	 */
	public WorldGenerator() {
		r = new Random();
	}

	/**
	 * perform generation
	 */
	public void generate() {
		float[] sphere = loadSphereMesh();
		Graph sphereGraph = convertToGraph(sphere);
		randomize(sphereGraph);
		smooth(sphereGraph);
		threshold(sphereGraph);
		Mesh planet = convertBack(sphereGraph);
		planetObject = instantiate(planet);
	}

	/**
	 * get the generated world data
	 *
	 * @return planet without water
	 */
	public MeshInstance getData() {

		return planetObject;
	}

	private float[] loadSphereMesh() {
		return ObjectLoader.loadObject("rawSphere6.obj");
	}

	private Graph convertToGraph(float[] sphereData) {
		Graph graph = new Graph();

		int vCount = sphereData.length / 8;
		System.err.println("vCount = " + vCount);
		int triCount = vCount / 3;
		System.err.println("triCount = " + triCount);

		for (int tri = 0; tri < triCount; tri++) {

			GraphNode[] edges = new GraphNode[3];
			for (int edge = 0; edge < 3; edge++) {
				int dataPointer = tri * (3 * 8) + edge * 8;
				Vector3f vPos = new Vector3f(sphereData[dataPointer], sphereData[dataPointer + 1], sphereData[dataPointer + 2]);
				edges[edge] = graph.getNode(vPos);
			}

			graph.connect(edges[0], edges[1]);
			graph.connect(edges[1], edges[2]);
			graph.connect(edges[2], edges[0]);

		}

		return graph;
	}

	private void randomize(Graph graph) {
		graph.getNodes().stream().parallel().forEach((GraphNode n) -> n.setNoiseValue(r.nextFloat() * 2 - 1));
	}

	private void smooth(Graph graph) {
		for (int loop = 0; loop < SMOOTH_LOOP_COUNT; loop++) {
			graph.getNodes().stream().forEach((GraphNode n) -> {
				List<GraphNode> conns = graph.getConnected(n);
				float accu = n.getNoiseValue();
				Iterator<GraphNode> cIt = conns.iterator();
				while (cIt.hasNext()) {
					accu += cIt.next().getNoiseValue();
				}
				accu /= (conns.size() + 1);

				n.setNoiseValue(accu);
				cIt = conns.iterator();
				while (cIt.hasNext()) {
					cIt.next().setNoiseValue(accu);
				}
			});
		}
	}

	private void threshold(Graph sphereGraph) {
		sphereGraph.getNodes().stream().parallel().forEach((GraphNode n) -> {
			n.setWater(n.getNoiseValue() < waterThreshold);
		});
	}

	private Mesh convertBack(Graph graph) {
		int DATA_STRIDE = 8;

		float[] points = new float[graph.getNodeCount() * DATA_STRIDE];
		for (int node = 0; node < graph.getNodeCount(); node++) {
			GraphNode graphNode = graph.getNodes().get(node);
			graphNode.setIndex(node);
			int dataPointer = node * DATA_STRIDE;

			float pointScale = getNodeHeight(graph, graphNode);

			Vector3f actualPos = Util.vScale(graphNode.getPosition(), pointScale);

			points[dataPointer++] = actualPos.x; //position data
			points[dataPointer++] = actualPos.y;
			points[dataPointer++] = actualPos.z;

			actualPos.normalise();

			points[dataPointer++] = actualPos.x; //normal data
			points[dataPointer++] = actualPos.y;
			points[dataPointer++] = actualPos.z;

			points[dataPointer++] = 0.25f + ((pointScale < landHeight) ? 0.5f : 0f);
			points[dataPointer++] = 0.5f;

		}

		int[] indizes = new int[graph.getConnectionCount() * 2];
		int indizesPointer = 0;

		//fetch all triangles
		List<GraphNode> allNodes = graph.getNodes();
		for (int node1i = 0; node1i < allNodes.size(); node1i++) {
			GraphNode node1 = allNodes.get(node1i);
			List<GraphNode> node1n = graph.getConnected(node1);
			
			for (GraphNode node2: node1n) {
				if (node2.getIndex() > node1.getIndex()) {
					
					List<GraphNode> node2n = graph.getConnected(node2);
					
					for (GraphNode node3: node2n) {
						if(node1n.contains(node3)) {
							if(node3.getIndex() > node2.getIndex()) {
								
								//we got a tri!
								
								indizes[indizesPointer++] = node1.getIndex();
								indizes[indizesPointer++] = node2.getIndex();
								indizes[indizesPointer++] = node3.getIndex();
								
							}
						}
					}
					
				}
			}
		}
		
		
		int[] vertexDataSizes = {3, 3, 2};
		Mesh planetMesh = new Mesh(points, indizes, vertexDataSizes);

		return planetMesh;
	}

	private MeshInstance instantiate(Mesh planet) {
		return new MeshInstance(planet, new Material("generatedWorld.png", "black.png"));
	}

	private void debugGraph(Graph g) {
		g.getNodes().stream().forEach(System.out::println);
	}

	private void debugCoast(Graph g) {
		long before = System.nanoTime();
		int vCount = g.getNodeCount();
		int coastCount = 0;
		List<GraphNode> vList = g.getNodes();
		for (GraphNode n : vList) {
			if (!n.isWater()) {
				boolean coast = false;
				List<GraphNode> conns = g.getConnected(n);
				for (GraphNode conn : conns) {
					if (conn.isWater()) {
						coast = true;
					}
				}
				if (coast) {
					coastCount++;
				}
			}
		}

		long after = System.nanoTime();
		//System.out.println("Time used: " + (after - before) / 1000000f + "ms");

		System.out.println("Coast percentage: " + (coastCount / (float) vCount * 100f));
	}

	/**
	 * calculate scale factor based on land/underwater/coast etc
	 *
	 * @param node a vertex point (GraphNode)
	 * @return its scale factor (distance from planet middle)
	 */
	private float getNodeHeight(Graph g, GraphNode node) {
		if (node.isWater()) {
			return underwaterHeight;
		} else {
			boolean coast = false;
			List<GraphNode> conns = g.getConnected(node);
			for (GraphNode c : conns) {
				if (c.isWater()) {
					coast = true;
				}
			}
			if (coast) {
				return waterHeight;
			} else {
				return landHeight;
			}
		}
	}

}
