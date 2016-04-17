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

	private static final int SMOOTH_LOOP_COUNT = 10;
	private static final float waterThreshold = 0f;
	private static final float landHeight = 1.06f;
	private static final float underwaterHeight = 0.85f;

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

		System.err.println(sphereGraph);

		randomize(sphereGraph);
		smooth(sphereGraph);
		threshold(sphereGraph);

		System.err.println("Smoothing complete");
		debugCoast(sphereGraph);

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

			points[dataPointer++] = 0.25f + ((pointScale < 1.001f) ? 0.5f : 0f);
			points[dataPointer++] = 0.5f;

		}

		int[] indizes = new int[graph.getConnectionCount() * 2];
		int indizesPointer = 0;

		//slowly de-construct graph to fetch triangles
		while (graph.getNodeCount() > 0) {
			GraphNode triStart = graph.getNodes().get(0); //get a node (forst of the list)
			List<GraphNode> triStartConnected = graph.getConnected(triStart);

			GraphNode tri2 = triStartConnected.get(0); //get one node connected to our fist node
			List<GraphNode> tri2connected = graph.getConnected(tri2);

			List<GraphNode> sharedNodes = new ArrayList<>();
			for (GraphNode triStartConn : triStartConnected) { //get a node connected to both of them
				if (tri2connected.contains(triStartConn)) {
					sharedNodes.add(triStartConn);
				}
			}
			if (sharedNodes.isEmpty()) { //there is no shared node?!?
				System.err.println("No De-triangulation possible. TriNodes:");
				System.err.println(triStart);
				System.err.println(tri2);
				System.exit(-1);
			}
			GraphNode tri3 = sharedNodes.get(0);
			System.err.println("Triangulation success:");
			System.err.println(triStart);
			System.err.println(tri2);
			System.err.println(tri3);

			//we got one triangle!
			//add triangle to vertex index list
			indizes[indizesPointer++] = triStart.getIndex();
			indizes[indizesPointer++] = tri2.getIndex();
			indizes[indizesPointer++] = tri3.getIndex();

			//remove tringle from graph (if needed)
			if (sharedNodes.size() == 1) {
				graph.removeConnection(triStart, tri2, true);
				graph.removeConnection(tri2, tri3, true);
				graph.removeConnection(tri3, triStart, true);
			}

		}

		int[] vertexDataSizes = {3, 3, 2};
		Mesh planetMesh = new Mesh(points, indizes, vertexDataSizes);

		return planetMesh;
	}

	private MeshInstance instantiate(Mesh planet) {
		return new MeshInstance(planet, new Material("white", "black"));
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
				return 1.0f;
			} else {
				return landHeight;
			}
		}
	}

}
