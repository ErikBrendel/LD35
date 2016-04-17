/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package generating;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.util.vector.Vector3f;

/**
 * An object representing nodes (with values each) and connections inbetween
 *
 * @author Erik
 */
public class Graph {

	/**
	 * set to true for faster results in connection-related functions
	 */
	public static final boolean REDUNDANT_CONNECTION_MODE = true;

	private ArrayList<GraphNode> nodes;
	private ArrayList<GraphConnection> connections;

	public Graph() {
		nodes = new ArrayList<>();
		connections = new ArrayList<>();
	}

	public void addNode(GraphNode node) {
		nodes.add(node);
	}

	public void addConnection(GraphConnection conn) {
		if (REDUNDANT_CONNECTION_MODE) {
			conn.n1.getConnected().add(conn.n2);
			conn.n2.getConnected().add(conn.n1);
		} else {
			connections.add(conn);
		}
	}

	public List<GraphConnection> getConnections() {
		if (REDUNDANT_CONNECTION_MODE) {
			System.err.println("getConnections() in REdundant mode not implemented!");
		}
		return connections;
	}

	public List<GraphNode> getNodes() {
		return nodes;
	}

	/**
	 * get the graphNode ad create a new one if there is none
	 *
	 * @param pos the 3d-position
	 * @return
	 */
	public GraphNode getNode(Vector3f pos) {
		return getNode(pos, true);
	}

	/**
	 * get the GraphNode object at the given Position (checked via equals)
	 *
	 * @param pos the position of the desired GraphNode
	 * @param createIfMissing wether to create a new GraphNode if there isn't
	 * any at this pos
	 * @return possibly null if createIfMising is false
	 */
	public GraphNode getNode(Vector3f pos, boolean createIfMissing) {
		GraphNode ret = null;

		Iterator<GraphNode> nodeI = nodes.iterator();
		while (nodeI.hasNext()) {
			GraphNode next = nodeI.next();
			if (next.position.equals(pos)) {
				ret = next;
				break;
			}
		}

		if (ret == null && createIfMissing) {
			ret = new GraphNode(pos);
			addNode(ret);
		}

		return ret;
	}

	/**
	 * connects two nodes if they are not connected yet
	 *
	 * @param n1
	 * @param n2
	 */
	public void connect(GraphNode n1, GraphNode n2) {
		if (REDUNDANT_CONNECTION_MODE) {
			if (!n1.getConnected().contains(n2)) {
				n1.getConnected().add(n2);
				n2.getConnected().add(n1);
			}
		} else {
			Iterator<GraphConnection> iterator = connections.iterator();
			while (iterator.hasNext()) {
				GraphConnection c = iterator.next();
				if (c.contains(n1) && c.getOther(n1) == n2) {
					return;
				}
			}
			addConnection(new GraphConnection(n1, n2));
		}
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public int getConnectionCount() {
		if (REDUNDANT_CONNECTION_MODE) {
			int count = 0;
			for (int n = 0; n < getNodeCount(); n++) {
				count += nodes.get(n).getConnected().size();
			}
			return count / 2;
		} else {
			return connections.size();
		}
	}

	/**
	 * returns a list of nodes connected to this node (not including n)
	 *
	 * Don't modify this list directly
	 *
	 * @param n a node
	 * @return a list of neighbour-nodes
	 */
	public List<GraphNode> getConnected(GraphNode n) {
		if (REDUNDANT_CONNECTION_MODE) {
			return n.getConnected();
		} else {
			ArrayList<GraphNode> conns = new ArrayList<>();
			Iterator<GraphConnection> cIt = connections.iterator();
			while (cIt.hasNext()) {
				GraphConnection c = cIt.next();
				if (c.contains(n)) {
					conns.add(c.getOther(n));
				}
			}
			return conns;
		}
	}

	/**
	 * removes a connection between two nodes (if present)
	 * 
	 * is able to delete any of those nodes if it became unconnected
	 * 
	 * @param n1
	 * @param n2
	 * @param deleteUnconnectedNodes wether to delete unconnected nodes
	 */
	public void removeConnection(GraphNode n1, GraphNode n2, boolean deleteUnconnectedNodes) {
		if (REDUNDANT_CONNECTION_MODE) {
			n1.getConnected().remove(n2);
			n2.getConnected().remove(n1);
			if (deleteUnconnectedNodes) {
				if (n1.getConnected().isEmpty()) {
					nodes.remove(n1);
				}
				if (n2.getConnected().isEmpty()) {
					nodes.remove(n2);
				}
			}
		} else {
			GraphConnection con = null;
			for (int c = 0; c < connections.size(); c++) {
				GraphConnection conn = connections.get(c);
				if (conn.contains(n1) && conn.getOther(n1) == n2) {
					con = conn;
					break;
				}
			}
			if(con == null) {
				return;
			}
			connections.remove(con);
			if (deleteUnconnectedNodes) {
				boolean n1UnConnected = true;
				boolean n2UnConnected = true;
				for (int c = 0; c < connections.size(); c++) {
					if(connections.get(c).contains(n1)) {
						n1UnConnected = false;
					}
					if(connections.get(c).contains(n2)) {
						n2UnConnected = false;
					}
					if(!n1UnConnected && !n2UnConnected) {
						break;
					}
				}
				if(n1UnConnected) {
					nodes.remove(n1);
				}
				if(n2UnConnected) {
					nodes.remove(n2);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Graph(" + getNodeCount() + " nodes, " + getConnectionCount() + " connections, average of " + (getConnectionCount() / (float) getNodeCount()) + "conns/node)";
	}

	public static class GraphNode {

		Vector3f position;
		float noiseValue;
		boolean water;
		int index;
		ArrayList<GraphNode> connected;

		public GraphNode(Vector3f position) {
			this.position = position;
			noiseValue = 0f;
			if (REDUNDANT_CONNECTION_MODE) {
				connected = new ArrayList<>();
			}
		}

		public Vector3f getPosition() {
			return position;
		}

		public void setNoiseValue(float noiseValue) {
			this.noiseValue = noiseValue;
		}

		public float getNoiseValue() {
			return noiseValue;
		}

		private ArrayList<GraphNode> getConnected() {
			return connected;
		}

		public void setWater(boolean water) {
			this.water = water;
		}

		public boolean isWater() {
			return water;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
		
		

		@Override
		public String toString() {
			return "GraphNode(" + getNoiseValue() + ", " + isWater() + ", " + getPosition() + ")";
		}

	}

	public static class GraphConnection {

		private GraphNode n1;
		private GraphNode n2;

		public GraphConnection(GraphNode n1, GraphNode n2) {
			this.n1 = n1;
			this.n2 = n2;
		}

		public GraphNode getN1() {
			return n1;
		}

		public GraphNode getN2() {
			return n2;
		}

		public boolean contains(GraphNode n) {
			return n1 == n || n2 == n;
		}

		public GraphNode getOther(GraphNode n) {
			return n1 == n ? n2 : n1;
		}
	}
}
